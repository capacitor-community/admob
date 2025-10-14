import { WebPlugin } from '@capacitor/core';

import type {
  AdMobPlugin,
  ApplicationMutedOptions,
  ApplicationVolumeOptions,
  AdmobConsentInfo,
  AdmobConsentRequestOptions,
} from '.';
import { AdmobConsentStatus } from './consent/consent-status.enum';
import { PrivacyOptionsRequirementStatus } from './consent/privacy-options-requirement-status.enum';
import type { AdMobRewardItem } from './reward';
import type { AdOptions, AdLoadInfo } from './shared';
import type { TrackingAuthorizationStatusInterface } from './shared/tracking-authorization-status.interface';

// ✅ New: Define a specific type for App Open Ad options
export interface AppOpenAdOptions {
  adId: string;        // The AdMob unit ID for the app open ad
  orientation?: 'portrait' | 'landscape'; // Optional orientation
  npa?: boolean;       // Optional: non-personalized ads flag
}

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
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

  async prepareInterstitial(options: AdOptions): Promise<AdLoadInfo> {
    console.log('prepareInterstitial', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showInterstitial(): Promise<void> {
    console.log('showInterstitial');
  }

  async prepareRewardVideoAd(options: AdOptions): Promise<AdLoadInfo> {
    console.log('prepareRewardVideoAd', options);
    return {
      adUnitId: options.adId,
    };
  }

  async showRewardVideoAd(): Promise<AdMobRewardItem> {
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

  async showRewardInterstitialAd(): Promise<AdMobRewardItem> {
    return {
      type: '',
      amount: 0,
    };
  }

  // ✅ Replaced "any" with AppOpenAdOptions type
  async loadAppOpen(options: AppOpenAdOptions): Promise<void> {
    console.log('loadAppOpen', options);
  }

  async showAppOpen(): Promise<void> {
    console.log('showAppOpen');
  }

  async isAppOpenLoaded(): Promise<{ value: boolean }> {
    return { value: false };
  }

  addListener(eventName: string, _listenerFunc: (...args: any[]) => void): any {
    console.log('addListener', eventName);
    return { remove: async () => {} };
  }
}
