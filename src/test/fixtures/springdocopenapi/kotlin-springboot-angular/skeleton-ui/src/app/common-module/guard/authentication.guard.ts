import {Injectable} from '@angular/core';
import {Router, UrlTree} from '@angular/router';
import {AuthStore} from '@common/store/auth.store';
import {firstValueFrom} from 'rxjs';
import {NAVIGATION} from '../constant/navigation';
import {AuthService} from '../service/auth.service';

@Injectable({
    providedIn: 'root',
})
export class AuthenticationGuard {
    public constructor(private readonly router: Router, private readonly authService: AuthService, private readonly authStore: AuthStore) {}

    public async canActivate(): Promise<boolean | UrlTree> {
        const token = await firstValueFrom(this.authStore.authToken$);

        if (token == null || token === '') {
            return this.router.parseUrl(NAVIGATION.Login);
        }

        const user = await firstValueFrom(this.authStore.user$);

        if (user == null) {
            try {
                await this.authService.refreshCurrentUser();
            } catch (e: unknown) {
                return this.router.parseUrl(NAVIGATION.Login);
            }
        }

        return true;
    }
}
