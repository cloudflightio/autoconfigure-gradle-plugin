import {TestBed} from '@angular/core/testing';
import {AuthenticationGuard} from '@common/guard/authentication.guard';
import {AuthStore} from '@common/store/auth.store';
import {AuthService} from '@common/service/auth.service';
import {Router} from '@angular/router';
import Mocked = jest.Mocked;
import {createMockForKeys} from '../../jest-mock';
import {NAVIGATION} from '@common/constant/navigation';
import {CurrentUserDto} from '@api';

describe('Authentication-Guard', () => {
  let guard: AuthenticationGuard;
  let authStoreSpy: Mocked<AuthStore>;
  let authServiceSpy: Mocked<AuthService>;
  let routerSpy: Mocked<Router>;

  beforeEach(() => {
    authStoreSpy = createMockForKeys<AuthStore>(['getCurrentUser', 'getAuthToken']);
    authServiceSpy = createMockForKeys<AuthService>(['refreshCurrentUser']);
    routerSpy = createMockForKeys<Router>(['parseUrl']);
    TestBed.configureTestingModule({
      providers: [
        {
          provide: Router,
          useFactory: () => routerSpy
        },
        {
          provide: AuthStore,
          useFactory: () => authStoreSpy
        },
        {
          provide: AuthService,
          useFactory: () => authServiceSpy
        },
        AuthenticationGuard,
      ]
    });
    guard = TestBed.inject(AuthenticationGuard);
  });

  test('Given a not logged in user the auth-guard should redirect to login', async () => {
    authStoreSpy.getAuthToken.mockImplementation(() => null);

    await guard.canActivate();

    expect(routerSpy.parseUrl).toBeCalledWith(NAVIGATION.Login);
  });

  test('Given a logged in user (token) with an empty user store it should redirect after refreshing the user ',
    async () => {
      authStoreSpy.getAuthToken.mockImplementation(() => 'token');
      authStoreSpy.getCurrentUser.mockImplementation(() => undefined);

      const isRedirecting = await guard.canActivate();

      expect(authServiceSpy.refreshCurrentUser).toBeCalled();
      expect(isRedirecting).toEqual(true);
    });

  test('Given a logged in user (token) with a loaded store it should redirect ',
    async () => {
      authStoreSpy.getAuthToken.mockImplementation(() => 'token');
      authStoreSpy.getCurrentUser.mockImplementation(() => ({} as CurrentUserDto));

      const isNavigationAllowed = await guard.canActivate();

      expect(authServiceSpy.refreshCurrentUser).not.toBeCalled();
      expect(isNavigationAllowed).toEqual(true);
    });

});
