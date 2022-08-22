import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Injectable()
export class AppInitService {
  public constructor(private translateService: TranslateService) {
  }

  public init(): void {
    // this language will be used as a fallback when a translation isn't found in the current language
    this.translateService.setDefaultLang('de');
    // the lang to use, if the lang isn't available, it will use the current loader to get them
    this.translateService.use('de');
  }
}
