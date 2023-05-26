/* eslint-disable @typescript-eslint/unbound-method */
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {AuthInterceptor} from '@common/interceptor/auth.interceptor';
import {AuthStore} from '@common/store/auth.store';
import {provideAutoSpy} from 'jest-auto-spies';
import {firstValueFrom, Observable, of, throwError} from 'rxjs';
import Mocked = jest.Mocked;

function getHttpHandlerMock<T>(status: number, error: boolean): Mocked<HttpHandler> {
    const httpEvent$ = error ? throwError(new HttpErrorResponse({status})) : of({status});

    return {
        handle: jest.fn().mockImplementation(() => httpEvent$ as Observable<HttpEvent<T>>),
    };
}

describe('Auth-Interceptor', () => {
    let interceptor: AuthInterceptor;
    let authStore: AuthStore;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [provideAutoSpy(Router), AuthInterceptor, AuthStore],
        });
        interceptor = TestBed.inject(AuthInterceptor);
        authStore = TestBed.inject(AuthStore);

        authStore.setCurrentUser({
            userName: 'test',
            name: 'test',
        });
        authStore.setAuthToken('test');
    });

    test.each([
        {status: 200, isError: false, shouldIntercept: false},
        {status: 422, isError: true, shouldIntercept: false},
        {status: 500, isError: true, shouldIntercept: false},
        {status: 401, isError: true, shouldIntercept: true},
        {status: 403, isError: true, shouldIntercept: true},
    ])('given $status it should intercept: $shouldIntercept', async ({status, isError, shouldIntercept}) => {
        const httpHandler = getHttpHandlerMock(status, isError);
        // eslint-disable-next-line rxjs/no-ignored-subscribe
        void interceptor.intercept(new HttpRequest('GET', '', {}), httpHandler).subscribe();

        if (shouldIntercept) {
            await expect(firstValueFrom(authStore.authToken$)).resolves.toEqual(undefined);
            await expect(firstValueFrom(authStore.user$)).resolves.toEqual(undefined);
        } else {
            await expect(firstValueFrom(authStore.authToken$)).resolves.toEqual('test');
            await expect(firstValueFrom(authStore.user$)).resolves.toEqual({
                userName: 'test',
                name: 'test',
            });
        }
    });
});
