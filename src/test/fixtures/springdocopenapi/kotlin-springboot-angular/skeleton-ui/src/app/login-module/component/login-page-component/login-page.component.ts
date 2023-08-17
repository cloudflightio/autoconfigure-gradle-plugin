import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Router} from '@angular/router';
import {NAVIGATION} from '@common/constant/navigation';
import {AuthService} from '@common/service/auth.service';

@Component({
    selector: 'app-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
    public username = '';
    public password = '';

    public constructor(private authService: AuthService, private router: Router) {}

    public async login(): Promise<void> {
        await this.authService.login(this.username, this.password);
        await this.router.navigateByUrl(NAVIGATION.Dashboard);
    }
}
