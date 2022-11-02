import {Injectable} from '@angular/core';
import {CanActivate, Router, UrlTree} from '@angular/router';
import {AuthStore} from '../store/auth.store';
import {AuthService} from '../service/auth.service';
import {NAVIGATION} from '../constant/navigation';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {

  public constructor(
    private router: Router,
    private authStore: AuthStore,
    private authService: AuthService
  ) {
  }

  public async canActivate(): Promise<boolean | UrlTree> {
    if (!this.authStore.getAuthToken()) {
      return this.router.parseUrl(NAVIGATION.Login);
    }
    if (!this.authStore.getCurrentUser()) {
      try {
        await this.authService.refreshCurrentUser();
      } catch (e: unknown) {
        return this.router.parseUrl(NAVIGATION.Login);
      }
    }
    return true;
  }

}
