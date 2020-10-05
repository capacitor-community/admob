import { WebPlugin } from '@capacitor/core';
import { AdMobPlugin, AdOptions } from './definitions';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web'],
    });
  }

  async initialize() {
    return {
      value: true,
    };
  }

  async showBanner(options: AdOptions) {
    console.log(options);
    return {
      value: true,
    };
  }

  // Hide the banner, remove it from screen, but can show it later
  async hideBanner() {
    return {
      value: true,
    };
  }

  // Resume the banner, show it after hide
  async resumeBanner() {
    return {
      value: true,
    };
  }

  // Destroy the banner, remove it from screen.
  async removeBanner() {
    return {
      value: true,
    };
  }

  async prepareInterstitial(options: AdOptions) {
    console.log(options);
    return {
      value: true,
    };
  }

  async showInterstitial() {
    return {
      value: true,
    };
  }

  async prepareRewardVideoAd(options: AdOptions) {
    console.log(options);
    return {
      value: true,
    };
  }

  async showRewardVideoAd() {
    return {
      value: true,
    };
  }

  async pauseRewardedVideo() {
    return {
      value: true,
    };
  }

  async resumeRewardedVideo() {
    return {
      value: true,
    };
  }

  async stopRewardedVideo() {
    return {
      value: true,
    };
  }
}

const AdMob = new AdMobWeb();

export { AdMob };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AdMob);
