import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Translation, TranslocoLoader} from '@ngneat/transloco';
import {Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class TranslocoHttpLoader implements TranslocoLoader {
    public constructor(private http: HttpClient) {}

    // eslint-disable-next-line rxjs/finnish
    public getTranslation(lang: string): Observable<Translation> {
        return this.http.get<Translation>(`/api/i18n/${lang}`);
    }
}
