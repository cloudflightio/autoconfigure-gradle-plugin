/* eslint-disable @typescript-eslint/unbound-method */
import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {NAVIGATION} from '@common/constant/navigation';
import {AuthenticationGuard} from '@common/guard/authentication.guard';
import {AuthService} from '@common/service/auth.service';
import {AuthStore} from '@common/store/auth.store';
import {provideAutoSpy, Spy} from 'jest-auto-spies';

describe('Authentication-Guard', () => {
    let guard: AuthenticationGuard;
    let authStore: AuthStore;
    let authServiceSpy: Spy<AuthService>;
    let routerSpy: Spy<Router>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [provideAutoSpy(Router), provideAutoSpy(AuthService), AuthenticationGuard, AuthStore],
        });
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        authServiceSpy = TestBed.inject<any>(AuthService);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        routerSpy = TestBed.inject<any>(Router);
        guard = TestBed.inject(AuthenticationGuard);
        authStore = TestBed.inject(AuthStore);
    });

    afterEach(() => {
        authStore.setAuthToken(undefined);
        authStore.setCurrentUser(undefined);
    });

    test('Given a not logged in user the auth-guard should redirect to login', async () => {
        await guard.canActivate();

        expect(routerSpy.parseUrl).toBeCalledWith(NAVIGATION.Login);
    });

    test('Given a logged in user (token) with an empty user store it should redirect after refreshing the user ', async () => {
        authStore.setAuthToken('token');
        authStore.setCurrentUser(undefined);

        const isRedirecting = await guard.canActivate();

        expect(authServiceSpy.refreshCurrentUser).toBeCalled();
        expect(isRedirecting).toEqual(true);
    });

    test('Given a logged in user (token) with a loaded store it should redirect ', async () => {
        authStore.setAuthToken('token');
        authStore.setCurrentUser({
            name: 'test',
            userName: 'test',
        });

        const isNavigationAllowed = await guard.canActivate();

        expect(authServiceSpy.refreshCurrentUser).not.toBeCalled();
        expect(isNavigationAllowed).toEqual(true);
    });
});
