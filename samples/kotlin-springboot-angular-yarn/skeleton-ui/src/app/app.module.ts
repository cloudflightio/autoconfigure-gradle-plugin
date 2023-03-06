import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ApiModule, BASE_PATH} from '@api';
import {AppInitService} from '@common/app-init/app-init.service';
import {HttpLoaderFactory} from '@common/app-init/http-loader-factory';
import {ClfCommonModule} from '@common/clf-common.module';
import {AuthInterceptor} from '@common/interceptor/auth.interceptor';
import {TokenInterceptor} from '@common/interceptor/token.interceptor';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {DashboardModule} from './dashboard-module/dashboard.module';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ApiModule,
    ReactiveFormsModule,
    AppRoutingModule,
    DashboardModule,
    ClfCommonModule
  ],
  providers: [
    AppInitService,
    {
      provide: APP_INITIALIZER,
      deps: [AppInitService, TranslateService],
      useFactory: (appInitService: AppInitService) => () => appInitService.init(),
      multi: true
    },
    {
      provide: BASE_PATH,
      useValue: '.'
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
    }],
  bootstrap: [AppComponent]
})
export class AppModule {
}
