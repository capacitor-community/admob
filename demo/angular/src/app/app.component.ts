import { Component } from '@angular/core';

import { IonApp, IonRouterOutlet, Platform } from '@ionic/angular/standalone';

import { AdMob } from '@capacitor-community/admob';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  imports: [IonApp, IonRouterOutlet],
  standalone: true,
})
export class AppComponent {
  constructor(private platform: Platform) {
    this.initializeApp();
  }

  initializeApp() {
    this.platform
      .ready()
      .then(() => {
        /**
         * initialize() require after platform.ready();
         */
        return AdMob.initialize({
          testingDevices: ['2077ef9a63d2b398840261c8221a0c9b'],
          initializeForTesting: true,
        });
      })
      .then(() => {
        return AdMob.setApplicationMuted({
          muted: false,
        });
      })
      .then(() => {
        return AdMob.setApplicationVolume({
          volume: 0.5,
        });
      });
  }
}
