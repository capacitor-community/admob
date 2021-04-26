import {Component, NgZone} from '@angular/core';
import {AdMob, RewardAdPluginEvents} from '@capacitor-community/admob';
import {ITestItems} from '../interfaces';
import {ViewDidEnter, ViewWillEnter, ViewWillLeave} from '@ionic/angular';
import {PluginListenerHandle} from '@capacitor/core';
import {rewardOptions} from '../ad.options';

const tryItems: ITestItems [] = [
  {
    type: 'method',
    name: 'prepareRewardVideoAd',
  },
  {
    type: 'method',
    name: 'showRewardVideoAd',
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Loaded
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Showed
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Rewarded
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Dismissed
  },
  {
    type: 'method',
    name: 'prepareRewardVideoAdFailed',
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.FailedToLoad
  },
];

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss']
})
export class Tab3Page  implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(
    private zone: NgZone,
    ) {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(RewardAdPluginEvents);
    eventKeys.forEach(key => {
      const handler = AdMob.addListener(RewardAdPluginEvents[key], (value) => {
        if (key === 'Dismissed') {
          AdMob.prepareRewardVideoAd({ adId: 'failed' })
            .then(async () => await this.updateItem('prepareRewardVideoAdFailed', true))
            .catch(async () => await this.updateItem('prepareRewardVideoAdFailed', false));
        }
        this.updateItem(RewardAdPluginEvents[key], true);
      });
      this.listenerHandlers.push(handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareRewardVideoAd(rewardOptions)
      .then(async () => await this.updateItem('prepareRewardVideoAd', true))
      .catch(async () => await this.updateItem('prepareRewardVideoAd', false));
    await AdMob.showRewardVideoAd()
      .then(async () => await this.updateItem('showRewardVideoAd', true))
      .catch(async () => await this.updateItem('showRewardVideoAd', false));
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
