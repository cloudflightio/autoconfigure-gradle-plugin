import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {TranslocoModule} from '@ngneat/transloco';
import {ButtonModule} from 'primeng/button';
import {RippleModule} from 'primeng/ripple';
import {NavbarComponent} from './component/navbar/navbar.component';

@NgModule({
    declarations: [NavbarComponent],
    imports: [CommonModule, TranslocoModule, RippleModule, ButtonModule],
    exports: [NavbarComponent],
})
export class ClfCommonModule {}
