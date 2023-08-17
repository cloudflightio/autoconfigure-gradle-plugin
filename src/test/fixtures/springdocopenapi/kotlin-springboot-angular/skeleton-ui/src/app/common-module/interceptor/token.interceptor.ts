import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {first, Observable, switchMap} from 'rxjs';
import {AuthStore} from '@common/store/auth.store';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
    public constructor(private readonly authStore: AuthStore) {}

    public intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        return this.authStore.authToken$.pipe(
            first(),
            switchMap((token) => {
                let req = request.clone();
                if (token != null && token !== '') {
                    req = request.clone({
                        setHeaders: {Authorization: 'Basic ' + token},
                    });
                }
                return next.handle(req);
            }),
        );
    }
}
