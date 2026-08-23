import { JsonPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  inject,
  signal,
  WritableSignal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AdMob,
  AdMobBannerSize,
  AdmobConsentDebugGeography,
  AdmobConsentStatus,
  AdMobRewardItem,
  AppOpenAdPluginEvents,
  BannerAdOptions,
  BannerAdPluginEvents,
  BannerAdSize,
  InterstitialAdPluginEvents,
  NativeAdFeed,
  NativeAdPluginEvents,
  NativeAdTemplate,
  RewardAdPluginEvents,
} from '@capacitor-community/admob';
import { Capacitor, PluginListenerHandle } from '@capacitor/core';
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
import {
  appOpenOptions,
  bannerBottomOptions,
  bannerTopOptions,
  interstitialOptions,
  rewardOptions,
} from '../shared/ad.options';
import { BannerViewportService } from '../shared/banner-viewport.service';
import { ViewModelStore } from '../shared/view-model-store';

interface AdEvent {
  name: string;
  value: unknown;
}

@Component({
  selector: 'app-demo',
  templateUrl: 'demo.html',
  styleUrl: 'demo.scss',
  imports: [
    FormsModule,
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
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Demo implements ViewWillEnter, ViewWillLeave {
  readonly vm = new ViewModel(this);

  ionViewWillEnter(): void {
    void this.vm.enter();
  }

  ionViewWillLeave(): void {
    void this.vm.leave();
  }
}

class ViewModel extends ViewModelStore<Demo> {
  readonly #toastController = inject(ToastController);
  readonly #bannerViewport = inject(BannerViewportService);
  readonly #listenerHandlers: PluginListenerHandle[] = [];
  #nativeAdFeed?: NativeAdFeed;

  #appMargin = 0;
  #bannerPosition: 'top' | 'bottom' = 'bottom';

  readonly bannerSizes = signal(Object.values(BannerAdSize));
  readonly currentBannerSize = signal<BannerAdSize | undefined>(undefined);
  readonly lastBannerEvent = signal<AdEvent | undefined>(undefined);
  readonly lastRewardEvent = signal<AdEvent | undefined>(undefined);
  readonly lastInterstitialEvent = signal<AdEvent | undefined>(undefined);
  readonly lastNativeAdEvent = signal<AdEvent | undefined>(undefined);
  readonly nativeAdsAvailable = Capacitor.isNativePlatform();
  readonly lastAppOpenEvent = signal<AdEvent | undefined>(undefined);
  readonly isConsentAvailable = signal(false);
  readonly isBannerPrepared = signal(false);
  readonly isRewardPrepared = signal(false);
  readonly isInterstitialPrepared = signal(false);
  readonly isAppOpenLoaded = signal(false);
  readonly isLoading = signal(false);

  async enter(): Promise<void> {
    const resizeHandler = await AdMob.addListener(BannerAdPluginEvents.SizeChanged, (info: AdMobBannerSize) => {
      this.#appMargin = info.height;
      this.#bannerViewport.setMargin(this.#appMargin, this.#bannerPosition);
    });
    this.#listenerHandlers.push(resizeHandler);

    const setupTasks = [
      this.#registerBannerListeners(),
      this.#registerRewardListeners(),
      this.#registerInterstitialListeners(),
      this.#registerAppOpenListeners(),
    ];
    if (this.nativeAdsAvailable) {
      setupTasks.push(this.#createNativeAdFeed());
    }
    await Promise.all(setupTasks);
  }

  async leave(): Promise<void> {
    await Promise.all([
      ...this.#listenerHandlers.splice(0).map((handler) => handler.remove()),
      this.#nativeAdFeed?.destroy(),
    ]);
    this.#nativeAdFeed = undefined;
  }

  async reloadNativeAd(): Promise<void> {
    await this.#didSucceed(
      this.#nativeAdFeed?.reload('demo-native-ad') ?? Promise.reject(new Error('Feed unavailable')),
    );
  }

  async requestConsentInfo(): Promise<void> {
    const consentInfo = await AdMob.requestConsentInfo({
      debugGeography: AdmobConsentDebugGeography.EEA,
      testDeviceIdentifiers: ['163FB114BEF1FC09FF772E930677A8D5'],
    });
    const isAvailable =
      consentInfo.status === AdmobConsentStatus.REQUIRED || consentInfo.status === AdmobConsentStatus.OBTAINED;
    this.isConsentAvailable.set(isAvailable);

    const message = isAvailable
      ? `Consent info found: ${JSON.stringify(consentInfo)}`
      : 'No consent info found. Create a consent message on the AdMob website first.';
    await this.#presentToast(message);
  }

  async showConsentForm(): Promise<void> {
    const result = await AdMob.showConsentForm().then(
      ({ status }) => ({ message: `Consent form showed with status: ${status}` }),
      (error: unknown) => ({ message: 'Error showing consent form. See logs.', error }),
    );
    if ('error' in result) {
      console.error('Error showing consent form', result.error);
    }
    await this.#presentToast(result.message, 'error' in result ? 'danger' : undefined);
  }

  async resetConsentInfo(): Promise<void> {
    const error = await AdMob.resetConsentInfo().then(
      () => undefined,
      (reason: unknown) => reason,
    );
    if (error !== undefined) {
      console.error('Error resetting consent info', error);
      await this.#presentToast('Error resetting consent info. See logs.', 'danger');
      return;
    }
    await this.#presentToast('Consent info has been reset. You can show a new consent form now.');
  }

  async showTopBanner(): Promise<void> {
    this.#bannerPosition = 'top';
    await this.#showBanner(bannerTopOptions);
  }

  async showBottomBanner(): Promise<void> {
    this.#bannerPosition = 'bottom';
    await this.#showBanner(bannerBottomOptions);
  }

  async hideBanner(): Promise<void> {
    const succeeded = await this.#didSucceed(AdMob.hideBanner());
    if (succeeded) {
      this.#bannerViewport.clearMargin();
    }
  }

  async resumeBanner(): Promise<void> {
    const succeeded = await this.#didSucceed(AdMob.resumeBanner());
    if (succeeded) {
      this.#bannerViewport.setMargin(this.#appMargin, this.#bannerPosition);
    }
  }

  async removeBanner(): Promise<void> {
    const succeeded = await this.#didSucceed(AdMob.removeBanner());
    if (succeeded) {
      this.#bannerViewport.clearMargin();
      this.#appMargin = 0;
      this.isBannerPrepared.set(false);
    }
  }

  async prepareInterstitial(): Promise<void> {
    this.isLoading.set(true);
    const succeeded = await this.#didSucceed(AdMob.prepareInterstitial(interstitialOptions));
    this.isInterstitialPrepared.set(succeeded);
    this.isLoading.set(false);
  }

  async showInterstitial(): Promise<void> {
    await this.#didSucceed(AdMob.showInterstitial());
    this.isInterstitialPrepared.set(false);
  }

  async prepareReward(): Promise<void> {
    this.isLoading.set(true);
    const succeeded = await this.#didSucceed(AdMob.prepareRewardVideoAd(rewardOptions));
    this.isRewardPrepared.set(succeeded);
    this.isLoading.set(false);
  }

  async showReward(): Promise<void> {
    const reward = await AdMob.showRewardVideoAd().then(
      (item) => item,
      () => undefined,
    );
    if (reward === undefined) {
      return;
    }
    await this.#presentRewardToast(reward);
    this.isRewardPrepared.set(false);
  }

  async loadAppOpen(): Promise<void> {
    this.isLoading.set(true);
    const succeeded = await this.#didSucceed(AdMob.loadAppOpen(appOpenOptions));
    this.isAppOpenLoaded.set(succeeded);
    this.isLoading.set(false);
  }

  async showAppOpen(): Promise<void> {
    await this.#didSucceed(AdMob.showAppOpen());
    this.isAppOpenLoaded.set(false);
  }

  async #showBanner(options: BannerAdOptions): Promise<void> {
    const succeeded = await this.#didSucceed(AdMob.showBanner({ ...options, adSize: this.currentBannerSize() }));
    this.isBannerPrepared.set(succeeded);
  }

  async #registerBannerListeners(): Promise<void> {
    const target = this.lastBannerEvent;
    const handlers = await Promise.all([
      AdMob.addListener(BannerAdPluginEvents.SizeChanged, (value) => this.#setEvent(target, 'SizeChanged', value)),
      AdMob.addListener(BannerAdPluginEvents.Loaded, () => this.#setEvent(target, 'Loaded')),
      AdMob.addListener(BannerAdPluginEvents.FailedToLoad, (value) => this.#setEvent(target, 'FailedToLoad', value)),
      AdMob.addListener(BannerAdPluginEvents.Opened, () => this.#setEvent(target, 'Opened')),
      AdMob.addListener(BannerAdPluginEvents.Closed, () => this.#setEvent(target, 'Closed')),
      AdMob.addListener(BannerAdPluginEvents.AdImpression, () => this.#setEvent(target, 'AdImpression')),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async #createNativeAdFeed(): Promise<void> {
    this.#nativeAdFeed = await NativeAdFeed.create({
      feedId: 'demo-native-feed',
      template: NativeAdTemplate.Medium,
      isTesting: true,
    });
    const target = this.lastNativeAdEvent;
    const handlers = await Promise.all([
      this.#nativeAdFeed.addListener(NativeAdPluginEvents.Loaded, (value) =>
        this.#setEvent(target, NativeAdPluginEvents.Loaded, value),
      ),
      this.#nativeAdFeed.addListener(NativeAdPluginEvents.FailedToLoad, (value) =>
        this.#setEvent(target, NativeAdPluginEvents.FailedToLoad, value),
      ),
      this.#nativeAdFeed.addListener(NativeAdPluginEvents.Clicked, (value) =>
        this.#setEvent(target, NativeAdPluginEvents.Clicked, value),
      ),
      this.#nativeAdFeed.addListener(NativeAdPluginEvents.AdImpression, (value) =>
        this.#setEvent(target, NativeAdPluginEvents.AdImpression, value),
      ),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async #registerRewardListeners(): Promise<void> {
    const target = this.lastRewardEvent;
    const handlers = await Promise.all([
      AdMob.addListener(RewardAdPluginEvents.FailedToLoad, (value) => this.#setEvent(target, 'FailedToLoad', value)),
      AdMob.addListener(RewardAdPluginEvents.Loaded, (value) => this.#setEvent(target, 'Loaded', value)),
      AdMob.addListener(RewardAdPluginEvents.Rewarded, (value) => this.#setEvent(target, 'Rewarded', value)),
      AdMob.addListener(RewardAdPluginEvents.Dismissed, () => this.#setEvent(target, 'Dismissed')),
      AdMob.addListener(RewardAdPluginEvents.FailedToShow, (value) => this.#setEvent(target, 'FailedToShow', value)),
      AdMob.addListener(RewardAdPluginEvents.Showed, () => this.#setEvent(target, 'Showed')),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async #registerInterstitialListeners(): Promise<void> {
    const target = this.lastInterstitialEvent;
    const handlers = await Promise.all([
      AdMob.addListener(InterstitialAdPluginEvents.FailedToLoad, (value) =>
        this.#setEvent(target, 'FailedToLoad', value),
      ),
      AdMob.addListener(InterstitialAdPluginEvents.Loaded, (value) => this.#setEvent(target, 'Loaded', value)),
      AdMob.addListener(InterstitialAdPluginEvents.Dismissed, () => this.#setEvent(target, 'Dismissed')),
      AdMob.addListener(InterstitialAdPluginEvents.FailedToShow, (value) =>
        this.#setEvent(target, 'FailedToShow', value),
      ),
      AdMob.addListener(InterstitialAdPluginEvents.Showed, () => this.#setEvent(target, 'Showed')),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async #registerAppOpenListeners(): Promise<void> {
    const target = this.lastAppOpenEvent;
    const handlers = await Promise.all([
      AdMob.addListener(AppOpenAdPluginEvents.Loaded, () => this.#setEvent(target, 'Loaded')),
      AdMob.addListener(AppOpenAdPluginEvents.FailedToLoad, (value) => this.#setEvent(target, 'FailedToLoad', value)),
      AdMob.addListener(AppOpenAdPluginEvents.Opened, () => this.#setEvent(target, 'Opened')),
      AdMob.addListener(AppOpenAdPluginEvents.Closed, () => this.#setEvent(target, 'Closed')),
      AdMob.addListener(AppOpenAdPluginEvents.FailedToShow, (value) => this.#setEvent(target, 'FailedToShow', value)),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  #setEvent(target: WritableSignal<AdEvent | undefined>, name: string, value?: unknown): void {
    target.set({ name, value });
  }

  async #didSucceed(operation: Promise<unknown>): Promise<boolean> {
    return operation.then(
      () => true,
      (error: unknown) => {
        console.error(error);
        return false;
      },
    );
  }

  async #presentRewardToast(reward: AdMobRewardItem): Promise<void> {
    await this.#presentToast(`Reward received: ${reward.amount} ${reward.type}.`);
  }

  async #presentToast(message: string, color?: string): Promise<void> {
    const toast = await this.#toastController.create({ message, duration: 3000, color });
    await toast.present();
  }
}
