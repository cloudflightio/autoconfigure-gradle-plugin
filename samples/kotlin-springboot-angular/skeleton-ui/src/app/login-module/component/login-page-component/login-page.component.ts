import {Component, HostBinding} from '@angular/core';
import {Router} from '@angular/router';
import {AuthService} from '@common/service/auth.service';
import {NAVIGATION} from '@common/constant/navigation';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss']
})
export class LoginPageComponent {
  @HostBinding('class') public classes = 'flex flex-auto';

  public username = '';
  public password = '';

  public constructor(
    private authService: AuthService,
    private router: Router
  ) {
  }

  public async login(): Promise<void> {
    await this.authService.login(this.username, this.password);
    await this.router.navigateByUrl(NAVIGATION.Dashboard);
  }

}
