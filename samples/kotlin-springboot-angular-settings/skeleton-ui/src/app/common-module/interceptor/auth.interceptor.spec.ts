import {HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {AuthStore} from '@common/store/auth.store';
import {AuthInterceptor} from '@common/interceptor/auth.interceptor';
import {createMockForKeys} from '../../jest-mock';
import {Observable, of, throwError} from 'rxjs';
import Mocked = jest.Mocked;

function getHttpHandlerMock<T>(status: number, error: boolean): HttpHandler {
  const httpEvent = error ? throwError({status}) : of({status});
  const mock: Mocked<HttpHandler> = createMockForKeys(['handle']);
  mock.handle.mockImplementation(() => httpEvent as Observable<HttpEvent<T>>);
  return mock;
}

describe('Auth-Interceptor', () => {
  let interceptor: AuthInterceptor;
  let authStoreSpy: Mocked<AuthStore>;
  let routerSpy: Mocked<Router>;

  beforeEach(() => {
    authStoreSpy = createMockForKeys<AuthStore>(['reset']);
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
        AuthInterceptor,
      ]
    });
    interceptor = TestBed.inject(AuthInterceptor);
  });

  test.each`
  status    |     isError     |     shouldIntercept
  ${200}    |     ${false}    |     ${false}
  ${422}    |     ${true}     |     ${false}
  ${500}    |     ${true}     |     ${false}
  ${401}    |     ${true}     |     ${true}
  ${403}    |     ${true}     |     ${true}
  `('Given $status it shouldIntercept: $shouldIntercept',
    ({status, isError, shouldIntercept}: { status: number; isError: boolean; shouldIntercept: boolean }) => {
      authStoreSpy.reset.mockImplementation(() => null);
      const httpHandler = getHttpHandlerMock(status, isError);
      interceptor.intercept({} as HttpRequest<unknown>, httpHandler).subscribe();

      if (shouldIntercept) {
        expect(authStoreSpy.reset).toBeCalled();
      } else {
        expect(authStoreSpy.reset).not.toBeCalled();
      }
    });

});
