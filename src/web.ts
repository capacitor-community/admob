import { WebPlugin } from '@capacitor/core';

import type { AdMobPlugin } from '.';
import type { AdMobRewardItem } from './reward';
import type { AdOptions, AdLoadInfo } from './shared';
import type { TrackingAuthorizationStatusInterface } from './shared/tracking-authorization-status.interface';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web'],
    });
  }

  async initialize(): Promise<void> {
    console.log('initialize');
  }

  async targetSettings(): Promise<void> {
    console.log('targetSettings');
  }

  async trackingAuthorizationStatus(): Promise<TrackingAuthorizationStatusInterface> {
    return {
      status: 'authorized',
    };
  }

  async showBanner(options: AdOptions): Promise<void> {
    console.log('showBanner', options);
  }

  // Hide the banner, remove it from screen, but can show it later
  async hideBanner(): Promise<void> {
    console.log('hideBanner');
  }

  // Resume the banner, show it after hide
  async resumeBanner(): Promise<void> {
    console.log('resumeBanner');
  }

  // Destroy the banner, remove it from screen.
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
    console.log(options);
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
}
