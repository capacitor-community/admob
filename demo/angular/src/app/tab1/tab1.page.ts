import { Component, OnInit, OnDestroy } from '@angular/core';
import { ViewDidEnter, ViewWillEnter, ViewWillLeave } from '@ionic/angular';
import {AdMob, BannerAdPluginEvents, RewardAdPluginEvents} from '@capacitor-community/admob';
import {bannerBottomOptions, bannerTopOptions} from '../ad-options';
import {PluginListenerHandle} from '@capacitor/core';

interface Items  {
  type: 'method' | 'event'
  name: string;
  result?: boolean
}

const tryItems: Items[] = [
  {
    type: 'method',
    name: 'showBanner',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.SizeChanged,
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.Loaded,
  },
  {
    type: 'method',
    name: 'hideBanner',
  },
  {
    type: 'method',
    name: 'resumeBanner',
  },
  {
    type: 'method',
    name: 'removeBanner',
  },
  {
    type: 'method',
    name: 'showBannerFailed',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.FailedToLoad,
  },
];

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss']
})
export class Tab1Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: Items[] = [];
  constructor() {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(BannerAdPluginEvents);
    eventKeys.forEach(key => {
      const handler = AdMob.addListener(BannerAdPluginEvents[key], (value) => {
        this.updateItem(BannerAdPluginEvents[key], true);
      });
      this.listenerHandlers.push(handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.showBanner(bannerBottomOptions)
      .then(async () => await this.updateItem('showBanner', true))
      .catch(async () => await this.updateItem('showBanner', false));

    await AdMob.hideBanner()
      .then(async () => await this.updateItem('hideBanner', true))
      .catch(async () => await this.updateItem('hideBanner', false));

    await AdMob.resumeBanner()
      .then(async () => await this.updateItem('resumeBanner', true))
      .catch(async () => await this.updateItem('resumeBanner', false));

    await AdMob.removeBanner()
      .then(async () => await this.updateItem('removeBanner', true))
      .catch(async () => await this.updateItem('removeBanner', false));

    await AdMob.showBanner({ adId: 'showBannerFailed' })
      .then(async () => await this.updateItem('showBannerFailed', true))
      .catch(async () => await this.updateItem('showBannerFailed', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }

  private async updateItem(name: string, result: boolean, time = 500) {
    this.eventItems = this.eventItems.map((item) => {
      if (item.name === name) {
        item.result = result;
      }
      return item;
    });
    await new Promise(resolve => setTimeout(() => resolve(), time));
  }
}
