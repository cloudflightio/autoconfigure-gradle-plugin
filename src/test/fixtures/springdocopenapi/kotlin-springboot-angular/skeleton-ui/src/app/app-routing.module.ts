import {inject, NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NAVIGATION} from '@common/constant/navigation';
import {AuthenticationGuard} from '@common/guard/authentication.guard';

const routes: Routes = [
    {
        path: NAVIGATION.Login,
        loadChildren: async () => (await import('./login-module/login.module')).LoginModule,
    },
    {
        path: NAVIGATION.Dashboard,
        canActivate: [async () => inject(AuthenticationGuard).canActivate()],
        loadChildren: async () => (await import('./dashboard-module/dashboard.module')).DashboardModule,
    },
    {
        path: '',
        pathMatch: 'full',
        redirectTo: NAVIGATION.Dashboard,
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})
export class AppRoutingModule {}
