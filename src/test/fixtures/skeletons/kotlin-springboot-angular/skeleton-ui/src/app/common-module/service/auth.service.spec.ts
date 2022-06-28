import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AuthStore} from '@common/store/auth.store';
import {AuthService} from '@common/service/auth.service';
import {createMockForKeys} from '../../jest-mock';
import {CurrentUserDto, UserService} from '@api';
import Mocked = jest.Mocked;
import {of} from 'rxjs';
import {HttpEvent} from '@angular/common/http';

describe('Auth-Service', () => {
  let service: AuthService;
  let authStoreSpy: Mocked<AuthStore>;
  let userService: Mocked<UserService>;

  beforeEach(() => {
    authStoreSpy = createMockForKeys<AuthStore>(['reset', 'getAuthToken', 'setCurrentUser']);
    userService = createMockForKeys<UserService>(['getCurrentUser']);
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: AuthStore,
          useFactory: () => authStoreSpy
        },
        {
          provide: UserService,
          useFactory: () => userService
        },
        AuthService
      ]
    });
    service = TestBed.inject(AuthService);
  });

  test('Logout it should reset the authStore', async () => {
    authStoreSpy.reset.mockImplementation(() => null);

    await service.logout();

    expect(authStoreSpy.reset).toBeCalled();
  });

  test('Refreshing the current user without token should throw', async () => {
    authStoreSpy.getAuthToken.mockImplementation(() => null);

    await expect(service.refreshCurrentUser).rejects.toThrow();
  });

  test('Refreshing the current user with token should fetch and set current user', async () => {
    const mockUser = {} as HttpEvent<CurrentUserDto>;
    authStoreSpy.getAuthToken.mockImplementation(() => 'token');
    authStoreSpy.setCurrentUser.mockImplementation(() => null);
    userService.getCurrentUser.mockImplementation(() => of(mockUser));

    await service.refreshCurrentUser();

    expect(authStoreSpy.setCurrentUser).toBeCalledWith(mockUser);
  });


});
