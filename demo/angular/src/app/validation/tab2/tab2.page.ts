import { Component } from '@angular/core';
import {AdMob, InterstitialAdPluginEvents} from '@capacitor-community/admob';
import {ITestItems} from '../../shared/interfaces';
import {PluginListenerHandle} from '@capacitor/core';
import {ViewDidEnter, ViewWillEnter, ViewWillLeave} from '@ionic/angular';
import {interstitialOptions} from '../../shared/ad.options';
import { HelperService } from '../../shared/helper.service';

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
    private helper: HelperService,
  ) {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(InterstitialAdPluginEvents);
    eventKeys.forEach(key => {
      const handler = AdMob.addListener(InterstitialAdPluginEvents[key], (value) => {
        this.helper.updateItem(this.eventItems,InterstitialAdPluginEvents[key], true);
        if (key === 'Dismissed') {
          AdMob.prepareInterstitial({ adId: 'failed' })
            .then(async () => await this.helper.updateItem(this.eventItems,'prepareInterstitialFailed', true))
            .catch(async () => await this.helper.updateItem(this.eventItems,'prepareInterstitialFailed', false));
        }
      });
      this.listenerHandlers.push(handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareInterstitial(interstitialOptions)
      .then(async () => await this.helper.updateItem(this.eventItems,'prepareInterstitial', true))
      .catch(async () => await this.helper.updateItem(this.eventItems,'prepareInterstitial', false));

    await AdMob.showInterstitial()
      .then(async () => await this.helper.updateItem(this.eventItems,'showInterstitial', true))
      .catch(async () => await this.helper.updateItem(this.eventItems,'showInterstitial', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }
}

