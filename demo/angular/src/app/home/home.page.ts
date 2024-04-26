import { Component, NgZone } from '@angular/core';
import { ViewWillEnter, ViewWillLeave } from '@ionic/angular';
import { PluginListenerHandle } from '@capacitor/core';
import { ToastController } from '@ionic/angular';

import {
  AdMob,
  AdMobBannerSize,
  AdMobRewardItem,
  BannerAdOptions,
  BannerAdPluginEvents,
  BannerAdSize,
  InterstitialAdPluginEvents,
  RewardAdPluginEvents,
  AdmobConsentInfo,
  AdmobConsentStatus,
  AdmobConsentDebugGeography,
} from '@capacitor-community/admob';
import { ReplaySubject } from 'rxjs';
import {
  bannerTopOptions,
  bannerBottomOptions,
  rewardOptions,
  interstitialOptions,
} from '../shared/ad.options';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage implements ViewWillEnter, ViewWillLeave {
  public readonly bannerSizes: BannerAdSize[] = Object.keys(
    BannerAdSize,
  ) as BannerAdSize[];
  public currentBannerSize?: BannerAdSize;

  private readonly lastBannerEvent$$ = new ReplaySubject<{
    name: string;
    value: any;
  }>(1);
  public readonly lastBannerEvent$ = this.lastBannerEvent$$.asObservable();

  private readonly lastRewardEvent$$ = new ReplaySubject<{
    name: string;
    value: any;
  }>(1);
  public readonly lastRewardEvent$ = this.lastRewardEvent$$.asObservable();

  private readonly lastInterstitialEvent$$ = new ReplaySubject<{
    name: string;
    value: any;
  }>(1);
  public readonly lastInterstitialEvent$ =
    this.lastInterstitialEvent$$.asObservable();

  private readonly listenerHandlers: PluginListenerHandle[] = [];
  /**
   * Height of AdSize
   */
  private appMargin = 0;
  private bannerPosition: 'top' | 'bottom';

  /**
   * For ion-item of template disabled
   */
  public isConsentAvailable = false;
  public isPrepareBanner = false;
  public isPrepareReward = false;
  public isPrepareInterstitial = false;

  public isLoading = false;

  constructor(
    private readonly toastCtrl: ToastController,
    private readonly ngZone: NgZone,
  ) {}

  async ionViewWillEnter() {
    /**
     * Run every time the Ad height changes.
     * AdMob cannot be displayed above the content, so create margin for AdMob.
     */
    const resizeHandler = await AdMob.addListener(
      BannerAdPluginEvents.SizeChanged,
      (info: AdMobBannerSize) => {
        this.appMargin = info.height;
        const app: HTMLElement = document.querySelector('ion-router-outlet');

        if (this.appMargin === 0) {
          app.style.marginTop = '';
          return;
        }

        if (this.appMargin > 0) {
          const body = document.querySelector('body');
          const bodyStyles = window.getComputedStyle(body);
          const safeAreaBottom = bodyStyles.getPropertyValue(
            '--ion-safe-area-bottom',
          );

          if (this.bannerPosition === 'top') {
            app.style.marginTop = this.appMargin + 'px';
          } else {
            app.style.marginBottom = `calc(${safeAreaBottom} + ${this.appMargin}px)`;
          }
        }
      },
    );

    this.listenerHandlers.push(resizeHandler);

    this.registerRewardListeners();
    this.registerBannerListeners();
    this.registerInterstitialListeners();
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach(handler => handler.remove());
  }

  /**
   * ==================== Consent ====================
   */

  async requestConsentInfo() {
    let consentInfo: AdmobConsentInfo = await AdMob.requestConsentInfo({
      debugGeography: AdmobConsentDebugGeography.EEA,
      testDeviceIdentifiers: ['163FB114BEF1FC09FF772E930677A8D5'],
    });

    if (
      consentInfo.status === AdmobConsentStatus.REQUIRED ||
      consentInfo.status === AdmobConsentStatus.OBTAINED
    ) {
      this.isConsentAvailable = true;

      const toast = await this.toastCtrl.create({
        message: `Consent info found: ${JSON.stringify(consentInfo)}`,
        duration: 3000,
      });
      await toast.present();
    } else {
      const toast = await this.toastCtrl.create({
        message: `No consent info found, please make sure you created it on Admob Website.`,
        duration: 3000,
      });
      await toast.present();
    }
  }

  async showConsentForm() {
    try {
      const { status } = await AdMob.showConsentForm();
      const toast = await this.toastCtrl.create({
        message: `Consent form showed with status: ${status}`,
        duration: 3000,
      });
      await toast.present();
    } catch (e) {
      const toast = await this.toastCtrl.create({
        message: `'Error on showConsentForm. See Logs`,
        duration: 3000,
        color: 'danger',
      });
      await toast.present();
      console.error('Error on showConsentForm', e);
    }
  }

  async resetConsentInfo() {
    try {
      await AdMob.resetConsentInfo();
      const toast = await this.toastCtrl.create({
        message: `Consent info have been reset. You can show new consent form now.`,
        duration: 3000,
      });
      await toast.present();
    } catch (e) {
      const toast = await this.toastCtrl.create({
        message: `Error on resetConsentInfo. See Logs`,
        duration: 3000,
        color: 'danger',
      });
      await toast.present();
      console.error('Error on resetConsentInfo', e);
    }
  }

  /**
   * ==================== BANNER ====================
   */
  async showTopBanner() {
    this.bannerPosition = 'top';
    await this.showBanner(bannerTopOptions);
  }

  async showBottomBanner() {
    this.bannerPosition = 'bottom';
    await this.showBanner(bannerBottomOptions);
  }

  private async showBanner(options: BannerAdOptions): Promise<void> {
    const bannerOptions: BannerAdOptions = {
      ...options,
      adSize: this.currentBannerSize,
    };
    console.log('Requesting banner with this options', bannerOptions);

    const result = await AdMob.showBanner(bannerOptions).catch(e =>
      console.error(e),
    );

    if (result === undefined) {
      return;
    }

    this.isPrepareBanner = true;
  }

  async hideBanner() {
    const result = await AdMob.hideBanner().catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    const app: HTMLElement = document.querySelector('ion-router-outlet');
    app.style.marginTop = '0px';
    app.style.marginBottom = '0px';
  }

  async resumeBanner() {
    const result = await AdMob.resumeBanner().catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    const app: HTMLElement = document.querySelector('ion-router-outlet');
    app.style.marginBottom = this.appMargin + 'px';
  }

  async removeBanner() {
    const result = await AdMob.removeBanner().catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    const app: HTMLElement = document.querySelector('ion-router-outlet');
    app.style.marginBottom = '0px';
    this.appMargin = 0;
    this.isPrepareBanner = false;
  }
  /**
   * ==================== /BANNER ====================
   */

  /**
   * ==================== REWARD ====================
   */
  async prepareReward() {
    try {
      const result = await AdMob.prepareRewardVideoAd(rewardOptions);
      console.log('Reward prepared', result);
      this.isPrepareReward = true;
    } catch (e) {
      console.error('There was a problem preparing the reward', e);
    } finally {
      this.isLoading = false;
    }
  }

  async showReward() {
    const result: AdMobRewardItem = await AdMob.showRewardVideoAd().catch(
      e => undefined,
    );
    if (result === undefined) {
      return;
    }
    const toast = await this.toastCtrl.create({
      message: `AdMob Reward received with currency: ${result.type}, amount ${result.amount}.`,
      duration: 2000,
    });
    await toast.present();

    this.isPrepareReward = false;
  }

  private registerInterstitialListeners(): void {
    const eventKeys = Object.keys(InterstitialAdPluginEvents);

    eventKeys.forEach(async key => {
      console.log(`registering ${InterstitialAdPluginEvents[key]}`);
      const handler = await AdMob.addListener(
        InterstitialAdPluginEvents[key],
        value => {
          console.log(`Interstitial Event "${key}"`, value);

          this.ngZone.run(() => {
            this.lastInterstitialEvent$$.next({ name: key, value: value });
          });
        },
      );
      this.listenerHandlers.push(handler);
    });
  }

  private registerRewardListeners(): void {
    const eventKeys = Object.keys(RewardAdPluginEvents);

    eventKeys.forEach(async key => {
      console.log(`registering ${RewardAdPluginEvents[key]}`);
      const handler = await AdMob.addListener(
        RewardAdPluginEvents[key],
        value => {
          console.log(`Reward Event "${key}"`, value);

          this.ngZone.run(() => {
            this.lastRewardEvent$$.next({ name: key, value: value });
          });
        },
      );
      this.listenerHandlers.push(handler);
    });
  }

  private registerBannerListeners(): void {
    const eventKeys = Object.keys(BannerAdPluginEvents);

    eventKeys.forEach(async key => {
      console.log(`registering ${BannerAdPluginEvents[key]}`);
      const handler = await AdMob.addListener(
        BannerAdPluginEvents[key],
        value => {
          console.log(`Banner Event "${key}"`, value);

          this.ngZone.run(() => {
            this.lastBannerEvent$$.next({ name: key, value: value });
          });
        },
      );
      this.listenerHandlers.push(handler);
    });
  }

  /**
   * ==================== /REWARD ====================
   */

  /**
   * ==================== Interstitial ====================
   */
  async prepareInterstitial() {
    this.isLoading = true;

    try {
      const result = await AdMob.prepareInterstitial(interstitialOptions);
      console.log('Interstitial Prepared', result);
      this.isPrepareInterstitial = true;
    } catch (e) {
      console.error('There was a problem preparing the Interstitial', e);
    } finally {
      this.isLoading = false;
    }
  }

  async showInterstitial() {
    await AdMob.showInterstitial().catch(e => console.log(e));

    this.isPrepareInterstitial = false;
  }

  /**
   * ==================== /Interstitial ====================
   */
}
