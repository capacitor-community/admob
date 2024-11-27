import { Component, NgZone } from '@angular/core';
import { AdMob, RewardAdPluginEvents } from '@capacitor-community/admob';
import { ITestItems } from '../../shared/interfaces';
import { ViewDidEnter, ViewWillEnter, ViewWillLeave } from '@ionic/angular';
import { PluginListenerHandle } from '@capacitor/core';
import { rewardOptions } from '../../shared/ad.options';
import { HelperService } from '../../shared/helper.service';

const tryItems: ITestItems[] = [
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
    name: RewardAdPluginEvents.Loaded,
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Showed,
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Rewarded,
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.Dismissed,
  },
  {
    type: 'method',
    name: 'prepareRewardVideoAdFailed',
  },
  {
    type: 'event',
    name: RewardAdPluginEvents.FailedToLoad,
    expect: 'error',
  },
];

@Component({
  selector: 'app-tab3',
  templateUrl: 'tab3.page.html',
  styleUrls: ['tab3.page.scss'],
  standalone: false,
})
export class Tab3Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(private helper: HelperService) {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(RewardAdPluginEvents);
    eventKeys.forEach(async key => {
      const handler = AdMob.addListener(RewardAdPluginEvents[key], value => {
        if (key === 'Dismissed') {
          AdMob.prepareRewardVideoAd({ adId: 'failed' })
            .then(
              async () =>
                await this.helper.updateItem(
                  this.eventItems,
                  'prepareRewardVideoAdFailed',
                  false,
                ),
            )
            .catch(
              async () =>
                await this.helper.updateItem(
                  this.eventItems,
                  'prepareRewardVideoAdFailed',
                  true,
                ),
            );
        }
        this.helper.updateItem(
          this.eventItems,
          RewardAdPluginEvents[key],
          true,
          value,
        );
      });
      this.listenerHandlers.push(await handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareRewardVideoAd(rewardOptions)
      .then(
        async data =>
          await this.helper.updateItem(
            this.eventItems,
            'prepareRewardVideoAd',
            !!data.adUnitId,
          ),
      )
      .catch(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'prepareRewardVideoAd',
            false,
          ),
      );
    await AdMob.showRewardVideoAd()
      .then(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'showRewardVideoAd',
            true,
          ),
      )
      .catch(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'showRewardVideoAd',
            false,
          ),
      );
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }
}
