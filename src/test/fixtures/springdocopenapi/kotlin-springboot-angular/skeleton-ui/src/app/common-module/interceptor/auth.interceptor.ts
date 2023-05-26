import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {AuthStore} from '@common/store/auth.store';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {NAVIGATION} from '../constant/navigation';

const unauthorizedCode = 401;
const forbiddenCode = 403;

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    public constructor(private readonly router: Router, private readonly authStore: AuthStore) {}

    public intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        return next.handle(request).pipe(
            catchError(async (error: unknown) => {
                if (!(error instanceof HttpErrorResponse)) {
                    throw error;
                }

                if (error.status === unauthorizedCode || error.status === forbiddenCode) {
                    this.authStore.reset();
                    await this.router.navigateByUrl(NAVIGATION.Login);
                }

                throw new Error(error.message);
            }),
        );
    }
}
