import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NAVIGATION} from '@common/constant/navigation';
import {AuthenticationGuard} from '@common/guard/authentication.guard';

const routes: Routes = [
  {
    path: NAVIGATION.Login,
    loadChildren: async () => import('./login-module/login.module').then((m) => m.LoginModule)
  },
  {
    path: NAVIGATION.Dashboard,
    canActivate: [AuthenticationGuard],
    loadChildren: async () => import('./dashboard-module/dashboard.module').then((m) => m.DashboardModule),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: NAVIGATION.Dashboard
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
