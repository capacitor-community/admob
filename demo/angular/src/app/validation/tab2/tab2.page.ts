import { Component } from '@angular/core';
import { AdMob, InterstitialAdPluginEvents } from '@capacitor-community/admob';
import { ITestItems } from '../../shared/interfaces';
import { PluginListenerHandle } from '@capacitor/core';
import {
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonTitle,
  IonToolbar,
  ViewDidEnter,
  ViewWillEnter,
  ViewWillLeave,
} from '@ionic/angular/standalone';
import { interstitialOptions } from '../../shared/ad.options';
import { HelperService } from '../../shared/helper.service';
import { addIcons } from 'ionicons';
import { checkmarkCircle, notificationsCircleOutline, playOutline } from 'ionicons/icons';

const tryItems: ITestItems[] = [
  {
    type: 'method',
    name: 'prepareInterstitial',
  },
  {
    type: 'method',
    name: 'showInterstitial',
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.Loaded,
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.Showed,
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.Dismissed,
  },
  {
    type: 'method',
    name: 'prepareInterstitialFailed',
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.FailedToLoad,
    expect: 'error',
  },
];

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss'],
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
})
export class Tab2Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(private helper: HelperService) {
    addIcons({ playOutline, notificationsCircleOutline, checkmarkCircle });
  }

  ionViewWillEnter() {
    const eventKeys = Object.keys(InterstitialAdPluginEvents);
    eventKeys.forEach(async (key) => {
      const eventName = InterstitialAdPluginEvents[key as keyof typeof InterstitialAdPluginEvents];
      const handler = AdMob.addListener(eventName as any, (value: unknown) => {
        this.helper.updateItem(this.eventItems, eventName, true, value);
        if (key === 'Dismissed') {
          AdMob.prepareInterstitial({ adId: 'failed' })
            .then(async () => await this.helper.updateItem(this.eventItems, 'prepareInterstitialFailed', false))
            .catch(async () => await this.helper.updateItem(this.eventItems, 'prepareInterstitialFailed', true));
        }
      });
      this.listenerHandlers.push(await handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareInterstitial(interstitialOptions)
      .then(async (data) => await this.helper.updateItem(this.eventItems, 'prepareInterstitial', !!data.adUnitId))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'prepareInterstitial', false));

    await AdMob.showInterstitial()
      .then(async () => await this.helper.updateItem(this.eventItems, 'showInterstitial', true))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'showInterstitial', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach((handler) => handler.remove());
  }
}
