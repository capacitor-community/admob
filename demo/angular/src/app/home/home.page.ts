import { Component, NgZone } from '@angular/core';
import {
  IonContent,
  IonFooter,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonSelect,
  IonSelectOption,
  IonSpinner,
  IonTitle,
  IonToolbar,
  ToastController,
  ViewWillEnter,
  ViewWillLeave,
} from '@ionic/angular/standalone';
import { PluginListenerHandle } from '@capacitor/core';

import {
  AdMob,
  AdMobBannerSize,
  AdmobConsentDebugGeography,
  AdmobConsentInfo,
  AdmobConsentStatus,
  AdMobRewardItem,
  BannerAdOptions,
  BannerAdPluginEvents,
  BannerAdSize,
  InterstitialAdPluginEvents,
  RewardAdPluginEvents,
} from '@capacitor-community/admob';
import { ReplaySubject } from 'rxjs';
import { bannerBottomOptions, bannerTopOptions, interstitialOptions, rewardOptions } from '../shared/ad.options';
import { FormsModule } from '@angular/forms';
import { AsyncPipe, JsonPipe } from '@angular/common';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  imports: [
    FormsModule,
    AsyncPipe,
    JsonPipe,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonSpinner,
    IonList,
    IonListHeader,
    IonLabel,
    IonItem,
    IonSelect,
    IonSelectOption,
    IonFooter,
  ],
})
export class HomePage implements ViewWillEnter, ViewWillLeave {
  public readonly bannerSizes: BannerAdSize[] = Object.keys(BannerAdSize) as BannerAdSize[];
  public currentBannerSize?: BannerAdSize;

  private readonly lastBannerEvent$$ = new ReplaySubject<{
    name: string;
    value: unknown;
  }>(1);
  public readonly lastBannerEvent$ = this.lastBannerEvent$$.asObservable();

  private readonly lastRewardEvent$$ = new ReplaySubject<{
    name: string;
    value: unknown;
  }>(1);
  public readonly lastRewardEvent$ = this.lastRewardEvent$$.asObservable();

  private readonly lastInterstitialEvent$$ = new ReplaySubject<{
    name: string;
    value: unknown;
  }>(1);
  public readonly lastInterstitialEvent$ = this.lastInterstitialEvent$$.asObservable();

  private readonly listenerHandlers: PluginListenerHandle[] = [];
  /**
   * Height of AdSize
   */
  private appMargin = 0;
  private bannerPosition: 'top' | 'bottom' = 'bottom';

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
    const resizeHandler = await AdMob.addListener(BannerAdPluginEvents.SizeChanged, (info: AdMobBannerSize) => {
      this.appMargin = info.height;
      const app = document.querySelector<HTMLElement>('ion-router-outlet')!;

      if (this.appMargin === 0) {
        app.style.marginTop = '';
        return;
      }

      if (this.appMargin > 0) {
        const body = document.querySelector<HTMLElement>('body')!;
        const bodyStyles = window.getComputedStyle(body);
        const safeAreaBottom = bodyStyles.getPropertyValue('--ion-safe-area-bottom');

        if (this.bannerPosition === 'top') {
          app.style.marginTop = this.appMargin + 'px';
        } else {
          app.style.marginBottom = `calc(${safeAreaBottom} + ${this.appMargin}px)`;
        }
      }
    });

    this.listenerHandlers.push(resizeHandler);

    this.registerRewardListeners();
    this.registerBannerListeners();
    this.registerInterstitialListeners();
  }

  ionViewWillLeave() {
    this.listenerHandlers.forEach((handler) => handler.remove());
  }

  /**
   * ==================== Consent ====================
   */

  async requestConsentInfo() {
    let consentInfo: AdmobConsentInfo = await AdMob.requestConsentInfo({
      debugGeography: AdmobConsentDebugGeography.EEA,
      testDeviceIdentifiers: ['163FB114BEF1FC09FF772E930677A8D5'],
    });

    if (consentInfo.status === AdmobConsentStatus.REQUIRED || consentInfo.status === AdmobConsentStatus.OBTAINED) {
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

    const result = await AdMob.showBanner(bannerOptions).catch((e) => console.error(e));

    if (result === undefined) {
      return;
    }

    this.isPrepareBanner = true;
  }

  async hideBanner() {
    const result = await AdMob.hideBanner().catch((e) => console.log(e));
    if (result === undefined) {
      return;
    }

    const app = document.querySelector<HTMLElement>('ion-router-outlet')!;
    app.style.marginTop = '0px';
    app.style.marginBottom = '0px';
  }

  async resumeBanner() {
    const result = await AdMob.resumeBanner().catch((e) => console.log(e));
    if (result === undefined) {
      return;
    }

    const app = document.querySelector<HTMLElement>('ion-router-outlet')!;
    app.style.marginBottom = this.appMargin + 'px';
  }

  async removeBanner() {
    const result = await AdMob.removeBanner().catch((e) => console.log(e));
    if (result === undefined) {
      return;
    }

    const app = document.querySelector<HTMLElement>('ion-router-outlet')!;
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
    const result: AdMobRewardItem | undefined = await AdMob.showRewardVideoAd().catch((e) => undefined);
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

    eventKeys.forEach(async (key) => {
      const eventName = InterstitialAdPluginEvents[key as keyof typeof InterstitialAdPluginEvents];
      console.log(`registering ${eventName}`);
      const handler = await AdMob.addListener(eventName as any, (value: unknown) => {
        console.log(`Interstitial Event "${key}"`, value);

        this.ngZone.run(() => {
          this.lastInterstitialEvent$$.next({ name: key, value: value });
        });
      });
      this.listenerHandlers.push(handler);
    });
  }

  private registerRewardListeners(): void {
    const eventKeys = Object.keys(RewardAdPluginEvents);

    eventKeys.forEach(async (key) => {
      const eventName = RewardAdPluginEvents[key as keyof typeof RewardAdPluginEvents];
      console.log(`registering ${eventName}`);
      const handler = await AdMob.addListener(eventName as any, (value: unknown) => {
        console.log(`Reward Event "${key}"`, value);

        this.ngZone.run(() => {
          this.lastRewardEvent$$.next({ name: key, value: value });
        });
      });
      this.listenerHandlers.push(handler);
    });
  }

  private registerBannerListeners(): void {
    const eventKeys = Object.keys(BannerAdPluginEvents);

    eventKeys.forEach(async (key) => {
      const eventName = BannerAdPluginEvents[key as keyof typeof BannerAdPluginEvents];
      console.log(`registering ${eventName}`);
      const handler = await AdMob.addListener(eventName as any, (value: unknown) => {
        console.log(`Banner Event "${key}"`, value);

        this.ngZone.run(() => {
          this.lastBannerEvent$$.next({ name: key, value: value });
        });
      });
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
    await AdMob.showInterstitial().catch((e) => console.log(e));

    this.isPrepareInterstitial = false;
  }

  /**
   * ==================== /Interstitial ====================
   */
}
