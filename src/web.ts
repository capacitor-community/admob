import { WebPlugin } from '@capacitor/core';
import { AdMobPlugin } from '.';
import { AdMobRewardItem } from './reward';
import { AdOptions } from './shared';


export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web'],
    });
  }

  async initialize(): Promise<void> {
    console.log();
  }

  async showBanner(options: AdOptions): Promise<void> {
    console.log(options);
  }

  // Hide the banner, remove it from screen, but can show it later
  async hideBanner(): Promise<void> {
    console.log();
  }

  // Resume the banner, show it after hide
  async resumeBanner(): Promise<void> {
    console.log();
  }

  // Destroy the banner, remove it from screen.
  async removeBanner(): Promise<void> {
    console.log();
  }

  async prepareInterstitial(options: AdOptions): Promise<void> {
    console.log(options);
  }

  async showInterstitial(): Promise<void> {
    console.log();
  }

  async prepareRewardVideoAd(options: AdOptions): Promise<void> {
    console.log(options);
  }

  async showRewardVideoAd(): Promise<AdMobRewardItem> {
    return {
      type: '',
      amount: 0,
    };
  }
}
