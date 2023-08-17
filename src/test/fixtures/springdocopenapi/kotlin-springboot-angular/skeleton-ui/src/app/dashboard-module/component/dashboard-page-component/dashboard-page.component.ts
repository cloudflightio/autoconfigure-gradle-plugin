import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';

@Component({
    selector: 'app-dashboard-page',
    templateUrl: './dashboard-page.component.html',
    styleUrls: ['./dashboard-page.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPageComponent {
    @HostBinding('class') public classes = 'flex flex-auto';
}
