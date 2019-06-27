import { WebPlugin } from '@capacitor/core';
import { AdMobPlugin, AdOptions } from './definitions';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web']
    });
  }

  async initialize() {
    return {
      value: null,
    }
  }


  async showBanner(options: AdOptions) {
    return {
      value: null,
    }
  }

  // Hide the banner, remove it from screen, but can show it later
  async hideBanner() {
    return {
      value: null,
    }
  }

  // Resume the banner, show it after hide
  async resumeBanner() {
    return {
      value: null,
    }
  }

  // Destroy the banner, remove it from screen.
  async removeBanner() {
    return {
      value: null,
    }
  }

  async prepareInterstitial(options: AdOptions) {
    return {
      value: null,
    }
  }

  async showInterstitial() {
    return {
      value: null,
    }
  }

  async prepareRewardVideoAd(options: AdOptions){
    return {
      value: null,
    }
  }

  async showRewardVideoAd(){
    return {
      value: null,
    }
  }

  async pauseRewardedVideo(){
    return {
      value: null,
    }
  }

  async resumeRewardedVideo(){
    return {
      value: null,
    }
  }

  async stopRewardedVideo(){
    return {
      value: null,
    }
  }
}

const AdMob = new AdMobWeb();

export { AdMob };
