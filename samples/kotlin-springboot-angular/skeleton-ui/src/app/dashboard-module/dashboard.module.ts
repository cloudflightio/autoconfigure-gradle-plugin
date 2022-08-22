import {NgModule} from '@angular/core';
import {DashboardPageComponent} from './component/dashboard-page-component/dashboard-page.component';
import {RouterModule, Routes} from '@angular/router';
import {ClfCommonModule} from '@common/clf-common.module';
import {CommonModule} from '@angular/common';

const routes: Routes = [
  {
    path: '',
    component: DashboardPageComponent
  }
];

@NgModule({
  declarations: [DashboardPageComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    ClfCommonModule,
  ]
})
export class DashboardModule {
}
