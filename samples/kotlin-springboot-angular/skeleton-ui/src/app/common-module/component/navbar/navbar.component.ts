import {Component} from '@angular/core';
import {CurrentUserDto} from '@api';
import {NAVIGATION} from '@common/constant/navigation';
import {Router} from '@angular/router';
import {AuthStore} from '@common/store/auth.store';
import {AuthService} from '@common/service/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {

  public constructor(
    private router: Router,
    private authStore: AuthStore,
    private authService: AuthService,
  ) {
  }

  public get currentUser(): CurrentUserDto | undefined {
    return this.authStore.getCurrentUser();
  }

  public async logout(): Promise<void> {
    await this.authService.logout();
    await this.router.navigateByUrl(NAVIGATION.Login);
  }

}
