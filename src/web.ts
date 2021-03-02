import { WebPlugin } from '@capacitor/core';
import { AdMobRewardItem, AdOptions } from './definitions';

import type { AdMobPlugin } from './definitions';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web'],
    });
  }

  async initialize() {}

  // @ts-ignore
  async showBanner(options: AdOptions) {
    console.log(options);
  }

  // Hide the banner, remove it from screen, but can show it later
  async hideBanner() {}

  // Resume the banner, show it after hide
  async resumeBanner() {}

  // Destroy the banner, remove it from screen.
  async removeBanner() {}

  async prepareInterstitial(options: AdOptions) {
    console.log(options);
  }

  async showInterstitial() {}

  async prepareRewardVideoAd(options: AdOptions) {
    console.log(options);
  }

  async showRewardVideoAd(): Promise<AdMobRewardItem> {
    return {
      type: '',
      amount: 0,
    };
  }
}
