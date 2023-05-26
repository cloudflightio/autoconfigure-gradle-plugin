import {CommonModule} from '@angular/common';
import {APP_INITIALIZER, NgModule, NgZone} from '@angular/core';
import {Logger, LoggerModule} from '@cloudflight/angular-logger';
import {environment} from 'src/environments/environment';

@NgModule({
    declarations: [],
    imports: [CommonModule, LoggerModule],
    exports: [],
    providers: [
        {
            provide: APP_INITIALIZER,
            useFactory: (logger: Logger, ngZone: NgZone) => () => {
                if (environment.production) {
                    window.addEventListener('error', (error) => {
                        logger.error('global', error);
                    });

                    // this subscription is ignored on purpose since it is global and
                    // should be only cleaned up when the app itself is destroyed
                    void ngZone.onError.subscribe((error) => {
                        logger.error('global', error);
                    });
                }
            },
            deps: [Logger, NgZone],
            multi: true,
        },
    ],
})
export class GlobalErrorModule {}
