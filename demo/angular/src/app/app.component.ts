import { Component } from '@angular/core';

import { Platform } from '@ionic/angular';

import { AdMob, AdMobInitializationOptions } from '@capacitor-community/admob';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  constructor(private platform: Platform) {
    this.initializeApp();
  }

  initializeApp() {
    this.platform.ready().then(() => {
      /**
       * initialize() require after platform.ready();
       */
      AdMob.initialize({
        requestTrackingAuthorization: true,
        testingDevices: ['2077ef9a63d2b398840261c8221a0c9b'],
        initializeForTesting: true,
      });

      AdMob.setApplicationMuted({
        muted: false,
      });

      AdMob.setApplicationVolume({
        volume: 0.5,
      });
    });
  }
}
