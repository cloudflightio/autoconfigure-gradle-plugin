/* eslint-disable @typescript-eslint/unbound-method */
import {TestBed} from '@angular/core/testing';
import {TokenInterceptor} from '@common/interceptor/token.interceptor';
import {HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import {AuthStore} from '@common/store/auth.store';
import {firstValueFrom, Observable, of} from 'rxjs';

describe('Token-Interceptor', () => {
    let interceptor: TokenInterceptor;
    let authStore: AuthStore;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [TokenInterceptor, AuthStore],
        });
        interceptor = TestBed.inject(TokenInterceptor);
        authStore = TestBed.inject(AuthStore);

        authStore.setAuthToken(undefined);
        authStore.setCurrentUser(undefined);
    });

    test('Given a stored token it should add it to the request header', async () => {
        const httpHandler: HttpHandler = {
            handle: jest.fn().mockImplementation(() => of({}) as Observable<HttpEvent<unknown>>),
        };
        const request = {clone: (req: HttpRequest<never>) => req} as HttpRequest<never>;
        authStore.setAuthToken('token');

        await firstValueFrom(interceptor.intercept(request, httpHandler));

        expect(httpHandler.handle).toBeCalledWith({
            setHeaders: {
                Authorization: 'Basic token',
            },
        });
    });

    test('Given no token stored it should add nothing to the request header', async () => {
        const httpHandler: HttpHandler = {
            handle: jest.fn().mockImplementation(() => of({}) as Observable<HttpEvent<unknown>>),
        };
        const request = {clone: (req: HttpRequest<never>) => req ?? {}} as HttpRequest<never>;
        authStore.setAuthToken();

        await firstValueFrom(interceptor.intercept(request, httpHandler));

        expect(httpHandler.handle).toBeCalledWith({});
    });
});
