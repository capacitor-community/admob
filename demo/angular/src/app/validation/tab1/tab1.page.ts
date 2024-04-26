import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { ViewDidEnter, ViewWillEnter, ViewWillLeave } from '@ionic/angular';
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';
import { bannerBottomOptions, bannerTopOptions } from '../../shared/ad.options';
import { PluginListenerHandle } from '@capacitor/core';
import { ITestItems } from '../../shared/interfaces';
import { HelperService } from '../../shared/helper.service';

const tryItems: ITestItems[] = [
  {
    type: 'method',
    name: 'trackingAuthorizationStatus',
    expect: ['authorized', 'denied', 'notDetermined', 'restricted'],
  },
  {
    type: 'method',
    name: 'showBanner',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.SizeChanged,
    expect: 1,
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
    type: 'event',
    name: BannerAdPluginEvents.SizeChanged,
    expect: 0,
  },
  {
    type: 'method',
    name: 'resumeBanner',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.SizeChanged,
    expect: 1,
  },
  {
    type: 'method',
    name: 'removeBanner',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.SizeChanged,
    expect: 0,
  },
  {
    type: 'method',
    name: 'showBannerFailed',
  },
  {
    type: 'event',
    name: BannerAdPluginEvents.FailedToLoad,
    expect: 'error',
  },
];

@Component({
  selector: 'app-tab1',
  templateUrl: 'tab1.page.html',
  styleUrls: ['tab1.page.scss'],
})
export class Tab1Page implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  private readonly listenerHandlers: PluginListenerHandle[] = [];
  public eventItems: ITestItems[] = [];
  constructor(private helper: HelperService) {}

  ionViewWillEnter() {
    const eventKeys = Object.keys(BannerAdPluginEvents);
    eventKeys.forEach(async key => {
      const handler = AdMob.addListener(BannerAdPluginEvents[key], value => {
        this.helper.updateItem(
          this.eventItems,
          BannerAdPluginEvents[key],
          true,
          value,
        );
      });
      this.listenerHandlers.push(await handler);
    });

    this.eventItems = JSON.parse(JSON.stringify(tryItems));
  }

  async ionViewDidEnter() {
    await AdMob.trackingAuthorizationStatus()
      .then(
        async d =>
          await this.helper.updateItem(
            this.eventItems,
            'trackingAuthorizationStatus',
            undefined,
            d.status,
          ),
      )
      .catch(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'trackingAuthorizationStatus',
            false,
          ),
      );

    await AdMob.showBanner(bannerBottomOptions)
      .then(
        async () =>
          await this.helper.updateItem(this.eventItems, 'showBanner', true),
      )
      .catch(
        async () =>
          await this.helper.updateItem(this.eventItems, 'showBanner', false),
      );

    await AdMob.hideBanner()
      .then(
        async () =>
          await this.helper.updateItem(this.eventItems, 'hideBanner', true),
      )
      .catch(
        async () =>
          await this.helper.updateItem(this.eventItems, 'hideBanner', false),
      );

    await AdMob.resumeBanner()
      .then(
        async () =>
          await this.helper.updateItem(this.eventItems, 'resumeBanner', true),
      )
      .catch(
        async () =>
          await this.helper.updateItem(this.eventItems, 'resumeBanner', false),
      );

    await AdMob.removeBanner()
      .then(
        async () =>
          await this.helper.updateItem(this.eventItems, 'removeBanner', true),
      )
      .catch(
        async () =>
          await this.helper.updateItem(this.eventItems, 'removeBanner', false),
      );

    await AdMob.showBanner({ adId: 'showBannerFailed' })
      .then(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'showBannerFailed',
            true,
          ),
      )
      .catch(
        async () =>
          await this.helper.updateItem(
            this.eventItems,
            'showBannerFailed',
            false,
          ),
      );
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }
}
