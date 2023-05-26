import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ApiModule, BASE_PATH} from '@api';
import {createConsoleConsumer, LoggerModule} from '@cloudflight/angular-logger';
import {ClfCommonModule} from '@common/clf-common.module';
import {AuthInterceptor} from '@common/interceptor/auth.interceptor';
import {TokenInterceptor} from '@common/interceptor/token.interceptor';
import {TranslocoRootModule} from '@common/translation/transloco-root.module';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {DashboardModule} from './dashboard-module/dashboard.module';
import {GlobalErrorModule} from './global-error-module/global-error.module';

@NgModule({
    declarations: [AppComponent],
    imports: [
        TranslocoRootModule,
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        ApiModule,
        ReactiveFormsModule,
        AppRoutingModule,
        DashboardModule,
        ClfCommonModule,
        LoggerModule.forRoot({
            consumers: [createConsoleConsumer()],
        }),
        GlobalErrorModule,
    ],
    providers: [
        {
            provide: BASE_PATH,
            useValue: '.',
        },
        {
            provide: HTTP_INTERCEPTORS,
            multi: true,
            useClass: TokenInterceptor,
        },
        {
            provide: HTTP_INTERCEPTORS,
            multi: true,
            useClass: AuthInterceptor,
        },
    ],
    bootstrap: [AppComponent],
})
export class AppModule {}
