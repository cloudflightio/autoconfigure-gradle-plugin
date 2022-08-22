import {AuthStore} from '@common/store/auth.store';
import {createMockForKeys} from '../../jest-mock';
import {TestBed} from '@angular/core/testing';
import {TokenInterceptor} from '@common/interceptor/token.interceptor';
import {HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import Mocked = jest.Mocked;

function getHttpHandlerMock<T>(): HttpHandler {
  const mock: Mocked<HttpHandler> = createMockForKeys(['handle']);
  mock.handle.mockImplementation(() => ((of({}) as Observable<HttpEvent<T>>)));
  return mock;
}

describe('Token-Interceptor', () => {
  let interceptor: TokenInterceptor;
  let authStoreSpy: Mocked<AuthStore>;

  beforeEach(() => {
    authStoreSpy = createMockForKeys<AuthStore>(['getAuthToken']);
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthStore,
          useFactory: () => authStoreSpy
        },
        TokenInterceptor,
      ]
    });
    interceptor = TestBed.inject(TokenInterceptor);
  });

  test('Given a stored token it should add it to the request header', () => {
    const httpHandler = getHttpHandlerMock();
    const request = {clone: (req: HttpRequest<never>) => req} as HttpRequest<never>;
    authStoreSpy.getAuthToken.mockImplementation(() => 'token');
    interceptor.intercept(request, httpHandler).subscribe();

    expect(httpHandler.handle).toBeCalledWith({
      setHeaders: {
        Authorization: 'Basic token'
      }
    });
  });

  test('Given no token stored it should add nothing to the request header', () => {
    const httpHandler = getHttpHandlerMock();
    const request = {clone: (req: HttpRequest<never>) => req ?? {}} as HttpRequest<never>;
    authStoreSpy.getAuthToken.mockImplementation(() => null);
    interceptor.intercept(request, httpHandler).subscribe();

    expect(httpHandler.handle).toBeCalledWith({});
  });


});
