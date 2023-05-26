import {NgModule} from '@angular/core';
import {translocoConfig, TranslocoModule, TRANSLOCO_CONFIG, TRANSLOCO_LOADER} from '@ngneat/transloco';
import {environment} from 'src/environments/environment';
import {TranslocoHttpLoader} from './transloco-http-loader';

@NgModule({
    exports: [TranslocoModule],
    providers: [
        {
            provide: TRANSLOCO_CONFIG,
            useValue: translocoConfig({
                availableLangs: ['de'],
                defaultLang: 'de',
                // Remove this option if your application
                // doesn't support changing language in runtime.
                reRenderOnLangChange: true,
                prodMode: environment.production,
            }),
        },
        {provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader},
    ],
})
export class TranslocoRootModule {}
