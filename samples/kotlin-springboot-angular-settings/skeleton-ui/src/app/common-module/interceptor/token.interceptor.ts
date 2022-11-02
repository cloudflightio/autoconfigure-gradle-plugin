import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthStore} from '@common/store/auth.store';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  public constructor(
    private authStore: AuthStore
  ) {
  }

  public intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authStore.getAuthToken();
    let req = request.clone();
    if (token) {
      req = request.clone({
        setHeaders: {Authorization: 'Basic ' + token}
      });
    }
    return next.handle(req);
  }

}
