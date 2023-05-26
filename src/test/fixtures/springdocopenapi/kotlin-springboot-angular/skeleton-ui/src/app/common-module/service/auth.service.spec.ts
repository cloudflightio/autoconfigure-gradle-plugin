/* eslint-disable @typescript-eslint/unbound-method */
import {HttpEvent} from '@angular/common/http';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {CurrentUserDto, UserService} from '@api';
import {AuthService} from '@common/service/auth.service';
import {AuthStore} from '@common/store/auth.store';
import {provideAutoSpy, Spy} from 'jest-auto-spies';
import {firstValueFrom, of} from 'rxjs';

describe('Auth-Service', () => {
    let service: AuthService;
    let authStore: AuthStore;
    let userServiceSpy: Spy<UserService>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [provideAutoSpy(UserService), AuthService, AuthStore],
        });

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        userServiceSpy = TestBed.inject<any>(UserService);
        service = TestBed.inject(AuthService);
        authStore = TestBed.inject(AuthStore);

        authStore.setAuthToken(undefined);
        authStore.setCurrentUser(undefined);
    });

    test('Logout it should reset the authStore', async () => {
        authStore.setCurrentUser({
            name: 'test',
            userName: 'test',
        });

        await service.logout();

        await expect(firstValueFrom(authStore.user$)).resolves.toEqual(undefined);
    });

    test('Refreshing the current user without token should throw', async () => {
        await expect(service.refreshCurrentUser).rejects.toThrow();
    });

    test('Refreshing the current user with token should fetch and set current user', async () => {
        const mockUser = {} as HttpEvent<CurrentUserDto>;
        authStore.setAuthToken('token');

        userServiceSpy.getCurrentUser.mockImplementation(() => of(mockUser));

        await service.refreshCurrentUser();

        await expect(firstValueFrom(authStore.user$)).resolves.toEqual(mockUser);
    });
});
