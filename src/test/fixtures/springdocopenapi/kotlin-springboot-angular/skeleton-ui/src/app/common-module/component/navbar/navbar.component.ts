import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Router} from '@angular/router';
import {NAVIGATION} from '@common/constant/navigation';
import {AuthService} from '@common/service/auth.service';
import {AuthStore} from '@common/store/auth.store';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent {
    public readonly user$ = this.authStore.user$;

    public constructor(private router: Router, private authStore: AuthStore, private authService: AuthService) {}

    public async logout(): Promise<void> {
        await this.authService.logout();
        await this.router.navigateByUrl(NAVIGATION.Login);
    }
}
