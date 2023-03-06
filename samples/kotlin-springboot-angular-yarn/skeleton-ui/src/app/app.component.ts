import {Component} from '@angular/core';
import {AuthStore} from '@common/store/auth.store';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {

  public constructor(private authStore: AuthStore) {
  }

  /**
   * Hides the navigation bar during the login screen
   */
  public showNavBar(): boolean {
    return !!this.authStore.getCurrentUser();
  }
}
