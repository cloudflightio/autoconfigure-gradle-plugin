import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {ButtonModule} from 'primeng/button';
import {RippleModule} from 'primeng/ripple';
import {NavbarComponent} from './component/navbar/navbar.component';


@NgModule({
  declarations: [NavbarComponent],
  imports: [
    CommonModule,
    TranslateModule,
    RippleModule,
    ButtonModule
  ],
  exports: [
    NavbarComponent,
    TranslateModule
  ],
})
export class ClfCommonModule {
}
