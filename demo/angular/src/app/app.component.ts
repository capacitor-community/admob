import { Component } from '@angular/core';

import { Platform } from '@ionic/angular';

import { AdMob, AppOpenAdPluginEvents, AppOpenAdOptions } from '@capacitor-community/admob';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  standalone: false,
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
        testingDevices: ['2077ef9a63d2b398840261c8221a0c9b'],
        initializeForTesting: true,
      });

      AdMob.setApplicationMuted({
        muted: false,
      });

      AdMob.setApplicationVolume({
        volume: 0.5,
      });

      // Ejemplo de App Open Ad
      this.showAppOpenAd();
    });
  }

  async showAppOpenAd() {
    // Escuchar eventos
    AdMob.addListener(AppOpenAdPluginEvents.Loaded, () => {
      console.log('App Open Ad cargado');
    });
    AdMob.addListener(AppOpenAdPluginEvents.FailedToLoad, () => {
      console.log('Fallo al cargar App Open Ad');
    });
    AdMob.addListener(AppOpenAdPluginEvents.Opened, () => {
      console.log('App Open Ad abierto');
    });
    AdMob.addListener(AppOpenAdPluginEvents.Closed, () => {
      console.log('App Open Ad cerrado');
    });
    AdMob.addListener(AppOpenAdPluginEvents.FailedToShow, () => {
      console.log('Fallo al mostrar App Open Ad');
    });

    const options: AppOpenAdOptions = {
      adUnitId: 'TU_AD_UNIT_ID', // Reemplaza por tu ID real
      showOnColdStart: true,
      showOnForeground: true,
    };
    await AdMob.loadAppOpen(options);
    const { value } = await AdMob.isAppOpenLoaded();
    if (value) {
      await AdMob.showAppOpen();
    }
  }
}
