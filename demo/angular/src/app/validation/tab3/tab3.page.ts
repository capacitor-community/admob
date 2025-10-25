import { Component } from '@angular/core';
import { AdMob, RewardAdPluginEvents } from '@capacitor-community/admob';
import { ITestItems } from '../../shared/interfaces';
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
import { PluginListenerHandle } from '@capacitor/core';
import { rewardOptions } from '../../shared/ad.options';
import { HelperService } from '../../shared/helper.service';
import { addIcons } from 'ionicons';
import { checkmarkCircle, notificationsCircleOutline, playOutline } from 'ionicons/icons';

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
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
})
export class Tab3Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(private helper: HelperService) {
    addIcons({ playOutline, notificationsCircleOutline, checkmarkCircle });
  }

  ionViewWillEnter() {
    const eventKeys = Object.keys(RewardAdPluginEvents);
    eventKeys.forEach(async (key) => {
      const eventName = RewardAdPluginEvents[key as keyof typeof RewardAdPluginEvents];
      const handler = AdMob.addListener(eventName as any, (value: unknown) => {
        if (key === 'Dismissed') {
          AdMob.prepareRewardVideoAd({ adId: 'failed' })
            .then(async () => await this.helper.updateItem(this.eventItems, 'prepareRewardVideoAdFailed', false))
            .catch(async () => await this.helper.updateItem(this.eventItems, 'prepareRewardVideoAdFailed', true));
        }
        this.helper.updateItem(this.eventItems, eventName, true, value);
      });
      this.listenerHandlers.push(await handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.prepareRewardVideoAd(rewardOptions)
      .then(async (data) => await this.helper.updateItem(this.eventItems, 'prepareRewardVideoAd', !!data.adUnitId))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'prepareRewardVideoAd', false));
    await AdMob.showRewardVideoAd()
      .then(async () => await this.helper.updateItem(this.eventItems, 'showRewardVideoAd', true))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'showRewardVideoAd', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach((handler) => handler.remove());
  }
}
