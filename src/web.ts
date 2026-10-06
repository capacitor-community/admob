import { WebPlugin } from '@capacitor/core';

import type {
  AdMobPlugin,
  ApplicationMutedOptions,
  ApplicationVolumeOptions,
  AdmobConsentInfo,
  AdmobConsentRequestOptions,
} from '.';
import type { AppOpenAdOptions } from './app-open/app-open-ad-options.interface';
import { AdmobConsentStatus } from './consent/consent-status.enum';
import { PrivacyOptionsRequirementStatus } from './consent/privacy-options-requirement-status.enum';
import type { NativeAdDefinitions } from './native-ads/native-ad-definitions.interface';
import type { NativeAdCommandIdentity, NativeAdFeedSession } from './native-ads/native-ad-options.interface';
import type { NativeAdPlacementBatch } from './native-ads/native-ad-placement.interface';
import type { AdMobRewardItem } from './reward';
import type { AdOptions, AdLoadInfo, AdShowOptions } from './shared';
import type { TrackingAuthorizationStatusInterface } from './shared/tracking-authorization-status.interface';

export class AdMobWeb extends WebPlugin implements AdMobPlugin, NativeAdDefinitions {
  async initialize(): Promise<void> {
    console.log('initialize');
  }

  async requestTrackingAuthorization(): Promise<void> {
    console.log('requestTrackingAuthorization');
  }

  async trackingAuthorizationStatus(): Promise<TrackingAuthorizationStatusInterface> {
    return {
      status: 'authorized',
    };
  }

  async requestConsentInfo(options?: AdmobConsentRequestOptions): Promise<AdmobConsentInfo> {
    console.log('requestConsentInfo', options);
    return {
      status: AdmobConsentStatus.REQUIRED,
      isConsentFormAvailable: true,
      canRequestAds: true,
      privacyOptionsRequirementStatus: PrivacyOptionsRequirementStatus.REQUIRED,
    };
  }

  async showPrivacyOptionsForm(): Promise<void> {
    console.log('showPrivacyOptionsForm');
  }

  async showConsentForm(): Promise<AdmobConsentInfo> {
    console.log('showConsentForm');
    return {
      status: AdmobConsentStatus.REQUIRED,
      canRequestAds: true,
      privacyOptionsRequirementStatus: PrivacyOptionsRequirementStatus.REQUIRED,
    };
  }

  async resetConsentInfo(): Promise<void> {
    console.log('resetConsentInfo');
  }

  async setApplicationMuted(options: ApplicationMutedOptions): Promise<void> {
    console.log('setApplicationMuted', options);
  }

  async setApplicationVolume(options: ApplicationVolumeOptions): Promise<void> {
    console.log('setApplicationVolume', options);
  }

  async showBanner(options: AdOptions): Promise<void> {
    console.log('showBanner', options);
  }

  async hideBanner(): Promise<void> {
    console.log('hideBanner');
  }

  async resumeBanner(): Promise<void> {
    console.log('resumeBanner');
  }

  async removeBanner(): Promise<void> {
    console.log('removeBanner');
  }

  async startNativeAdFeed(): Promise<void> {
    throw new Error('Native ads are only available on iOS and Android');
  }

  async destroyNativeAdFeed(options: NativeAdFeedSession): Promise<void> {
    console.log('destroyNativeAdFeed', options);
  }

  async loadNativeAd(): Promise<void> {
    throw new Error('Native ads are only available on iOS and Android');
  }

  async updateNativeAdPlacements(options: NativeAdPlacementBatch): Promise<void> {
    console.log('updateNativeAdPlacements', options);
  }

  async removeNativeAd(options: NativeAdCommandIdentity): Promise<void> {
    console.log('removeNativeAd', options);
  }

  async prepareInterstitial(options: AdOptions): Promise<AdLoadInfo> {
    console.log('prepareInterstitial', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showInterstitial(options?: AdShowOptions): Promise<void> {
    console.log('showInterstitial', options);
  }

  async prepareRewardVideoAd(options: AdOptions): Promise<AdLoadInfo> {
    console.log('prepareRewardVideoAd', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showRewardVideoAd(options?: AdShowOptions): Promise<AdMobRewardItem> {
    console.log('showRewardVideoAd', options);
    return {
      type: '',
      amount: 0,
    };
  }

  async prepareRewardInterstitialAd(options: AdOptions): Promise<AdLoadInfo> {
    console.log('prepareRewardInterstitialAd', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showRewardInterstitialAd(options?: AdShowOptions): Promise<AdMobRewardItem> {
    console.log('showRewardInterstitialAd', options);
    return {
      type: '',
      amount: 0,
    };
  }

  async loadAppOpen(options: AppOpenAdOptions): Promise<AdLoadInfo> {
    console.log('loadAppOpen', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showAppOpen(options?: AdShowOptions): Promise<void> {
    console.log('showAppOpen', options);
  }

  async isAppOpenLoaded(): Promise<{ value: boolean }> {
    return { value: false };
  }

  override addListener(
    eventName: string,
    listenerFunc: (...args: any[]) => void,
  ): Promise<{ remove: () => Promise<void> }> {
    void listenerFunc;
    console.log('addListener', eventName);
    return Promise.resolve({ remove: () => Promise.resolve() });
  }
}
