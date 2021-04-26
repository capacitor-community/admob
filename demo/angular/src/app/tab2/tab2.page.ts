import { Component, NgZone } from '@angular/core';
import {AdMob, InterstitialAdPluginEvents} from '@capacitor-community/admob';
import {ITestItems} from '../interfaces';
import {PluginListenerHandle} from '@capacitor/core';
import {ViewDidEnter, ViewWillEnter, ViewWillLeave} from '@ionic/angular';
import {interstitialOptions} from '../ad.options';

const tryItems: ITestItems [] = [
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
    name: InterstitialAdPluginEvents.Loaded
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.Showed
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.Dismissed
  },
  {
    type: 'method',
    name: 'prepareInterstitialFailed',
  },
  {
    type: 'event',
    name: InterstitialAdPluginEvents.FailedToLoad
  },
];

@Component({
  selector: 'app-tab2',
  templateUrl: 'tab2.page.html',
  styleUrls: ['tab2.page.scss']
})
export class Tab2Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(
    private zone: NgZone,
  ) {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(InterstitialAdPluginEvents);
    eventKeys.forEach(key => {
      const handler = AdMob.addListener(InterstitialAdPluginEvents[key], (value) => {
        this.updateItem(InterstitialAdPluginEvents[key], true);
        if (key === 'Dismissed') {
          AdMob.prepareInterstitial({ adId: 'failed' })
            .then(async () => await this.updateItem('prepareInterstitialFailed', true))
            .catch(async () => await this.updateItem('prepareInterstitialFailed', false));
        }
      });
      this.listenerHandlers.push(handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareInterstitial(interstitialOptions)
      .then(async () => await this.updateItem('prepareInterstitial', true))
      .catch(async () => await this.updateItem('prepareInterstitial', false));

    await AdMob.showInterstitial()
      .then(async () => await this.updateItem('showInterstitial', true))
      .catch(async () => await this.updateItem('showInterstitial', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }

  private async updateItem(name: string, result: boolean, time = 500) {
    this.zone.run(() => {
      this.eventItems = this.eventItems.map((item) => {
        if (item.name === name) {
          item.result = result;
        }
        return item;
      });
    });
    await new Promise(resolve => setTimeout(() => resolve(), time));
  }
}

