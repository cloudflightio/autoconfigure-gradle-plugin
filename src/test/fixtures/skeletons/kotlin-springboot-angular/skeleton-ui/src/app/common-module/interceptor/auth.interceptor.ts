import {Injectable} from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Router} from '@angular/router';
import {catchError} from 'rxjs/operators';
import {NAVIGATION} from '../constant/navigation';
import {AuthStore} from '@common/store/auth.store';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  public constructor(
    private router: Router,
    private authStore: AuthStore
  ) {
  }

  public intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(catchError(async (error) => this.handleAuthError(error)));
  }

  private async handleAuthError(err: HttpErrorResponse): Promise<HttpResponse<unknown>> {
    if (err.status === 401 || err.status === 403) {
      this.authStore.reset();
      await this.router.navigateByUrl(NAVIGATION.Login);
    }
    throw new Error(err.message);
  }
}
