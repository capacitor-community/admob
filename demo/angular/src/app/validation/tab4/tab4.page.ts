import { Component } from '@angular/core';
import { AdMob, AppOpenAdPluginEvents } from '@capacitor-community/admob';
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
import { addIcons } from 'ionicons';
import { checkmarkCircle, notificationsCircleOutline, playOutline } from 'ionicons/icons';
import { appOpenOptions } from '../../shared/ad.options';
import { HelperService } from '../../shared/helper.service';
import { ITestItems } from '../../shared/interfaces';

const tryItems: ITestItems[] = [
  {
    type: 'method',
    name: 'loadAppOpen',
  },
  {
    type: 'event',
    name: AppOpenAdPluginEvents.Loaded,
  },
  {
    type: 'method',
    name: 'isAppOpenLoaded',
  },
  {
    type: 'method',
    name: 'showAppOpen',
  },
  {
    type: 'event',
    name: AppOpenAdPluginEvents.Opened,
  },
  {
    type: 'method',
    name: 'showAppOpenFailed',
  },
  {
    type: 'event',
    name: AppOpenAdPluginEvents.FailedToShow,
    expect: 'error',
  },
  {
    type: 'event',
    name: AppOpenAdPluginEvents.Closed,
  },
  {
    type: 'method',
    name: 'loadAppOpenFailed',
  },
  {
    type: 'event',
    name: AppOpenAdPluginEvents.FailedToLoad,
    expect: 'error',
  },
];

@Component({
  selector: 'app-tab4',
  templateUrl: 'tab4.page.html',
  styleUrls: ['tab4.page.scss'],
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
})
export class Tab4Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];

  constructor(private helper: HelperService) {
    addIcons({ playOutline, notificationsCircleOutline, checkmarkCircle });
  }

  ionViewWillEnter() {
    const eventKeys = Object.keys(AppOpenAdPluginEvents);
    eventKeys.forEach(async (key) => {
      const eventName = AppOpenAdPluginEvents[key as keyof typeof AppOpenAdPluginEvents];
      const handler = AdMob.addListener(eventName as any, (value: unknown) => {
        this.helper.updateItem(this.eventItems, eventName, true, value);

        if (key === 'Opened') {
          AdMob.showAppOpen()
            .then(async () => await this.helper.updateItem(this.eventItems, 'showAppOpenFailed', false))
            .catch(async () => await this.helper.updateItem(this.eventItems, 'showAppOpenFailed', true));
        }

        if (key === 'Closed') {
          AdMob.loadAppOpen({ adId: 'failed' })
            .then(async () => await this.helper.updateItem(this.eventItems, 'loadAppOpenFailed', false))
            .catch(async () => await this.helper.updateItem(this.eventItems, 'loadAppOpenFailed', true));
        }
      });
      this.listenerHandlers.push(await handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.loadAppOpen(appOpenOptions)
      .then(async () => await this.helper.updateItem(this.eventItems, 'loadAppOpen', true))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'loadAppOpen', false));

    await AdMob.isAppOpenLoaded()
      .then(async ({ value }) => await this.helper.updateItem(this.eventItems, 'isAppOpenLoaded', value))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'isAppOpenLoaded', false));

    await AdMob.showAppOpen()
      .then(async () => await this.helper.updateItem(this.eventItems, 'showAppOpen', true))
      .catch(async () => await this.helper.updateItem(this.eventItems, 'showAppOpen', false));
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach((handler) => handler.remove());
  }
}
