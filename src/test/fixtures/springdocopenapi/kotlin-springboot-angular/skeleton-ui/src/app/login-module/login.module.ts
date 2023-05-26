import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';
import {ClfCommonModule} from '@common/clf-common.module';
import {TranslocoModule} from '@ngneat/transloco';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {PasswordModule} from 'primeng/password';
import {RippleModule} from 'primeng/ripple';
import {LoginPageComponent} from './component/login-page-component/login-page.component';

const routes: Routes = [
    {
        path: '',
        component: LoginPageComponent,
    },
];

@NgModule({
    declarations: [LoginPageComponent],
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        FormsModule,
        ClfCommonModule,
        RippleModule,
        ButtonModule,
        InputTextModule,
        PasswordModule,
        TranslocoModule,
    ],
})
export class LoginModule {}
