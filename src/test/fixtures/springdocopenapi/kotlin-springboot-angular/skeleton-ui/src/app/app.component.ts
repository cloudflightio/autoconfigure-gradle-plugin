import {ChangeDetectionStrategy, Component} from '@angular/core';
import {AuthStore} from '@common/store/auth.store';
import {map} from 'rxjs';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
    /**
     * Hides the navigation bar during the login screen
     */
    public readonly shouldShowNavbar$ = this.authStore.user$.pipe(map((user) => user != null));

    public constructor(private readonly authStore: AuthStore) {}
}
