import { Component, OnInit, OnDestroy } from '@angular/core';
import {PluginListenerHandle, Plugins} from '@capacitor/core';
import { ToastController } from '@ionic/angular';

import { AdOptions, AdSize, AdPosition, AdMobRewardItem } from '@capacitor-community/admob';
const { AdMob } = Plugins;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage implements OnInit, OnDestroy {
  /**
   * Height of AdSize
   */
  private appMargin = 0;
  private bannerPosition: 'top' | 'bottom';

  /**
   * For ion-item of template disabled
   */
  public isPrepareBanner = false;
  public isPrepareReward = false;
  public isPrepareInterstitial = false;

  /**
   * Setting of Ads
   */
  private bannerTopOptions: AdOptions = {
    adId: 'ca-app-pub-3940256099942544/2934735716',
    adSize: AdSize.SMART_BANNER,
    position: AdPosition.TOP_CENTER,
    // npa: false,
  };

  private bannerBottomOptions: AdOptions = {
    adId: 'ca-app-pub-3940256099942544/2934735716',
    adSize: AdSize.SMART_BANNER,
    position: AdPosition.BOTTOM_CENTER,
    npa: true,
  };

  private rewardOptions: AdOptions = {
    adId: 'ca-app-pub-3940256099942544/5224354917',
  };

  private interstitialOptions: AdOptions = {
    adId: 'ca-app-pub-3940256099942544/1033173712',
  };

  /**
   * for EventListener
   */
  private eventOnAdSize: PluginListenerHandle;
  private eventPrepareReward: PluginListenerHandle
  private eventRewardReceived: AdMobRewardItem;

  public isLoading = false;

  constructor(
    private toastCtrl: ToastController,
  ) {}

  ngOnInit() {
    /**
     * Run every time the Ad height changes.
     * AdMob cannot be displayed above the content, so create margin for AdMob.
     */
    this.eventOnAdSize = AdMob.addListener('onAdSize', (info: any) => {
      this.appMargin = parseInt(info.height, 10);
      if (this.appMargin > 0) {
        const app: HTMLElement = document.querySelector('ion-router-outlet');
        if (this.bannerPosition === 'top') {
          app.style.marginTop = this.appMargin + 'px';
        } else {
          app.style.marginBottom = this.appMargin + 'px';
        }
      }
    });

    AdMob.addListener('onInterstitialAdLoaded', (info) => {
      this.isPrepareInterstitial = true;
    });

    /**
     * RewardedVideo ad
     */
    this.eventPrepareReward = AdMob.addListener('onRewardedVideoAdLoaded', (info: boolean) => {
      this.isPrepareReward = true;
      this.isLoading = false;
    });

    AdMob.addListener('onRewarded', (info) => {
      this.eventRewardReceived = info;
    });

    AdMob.addListener('onRewardedVideoAdClosed', async (info) => {
      if (this.eventRewardReceived) {
        const toast = await this.toastCtrl.create({
          message: `AdMob Reward received with currency: ${this.eventRewardReceived.type}, amount ${this.eventRewardReceived.amount}.`,
          duration: 2000,
        });
        await toast.present();
      }
    });
  }

  ngOnDestroy() {
    if (this.eventOnAdSize) {
      this.eventOnAdSize.remove();
    }

    if (this.eventPrepareReward) {
      this.eventPrepareReward.remove();
    }
  }

  /**
   * ==================== BANNER ====================
   */
  async showTopBanner() {
    this.bannerPosition = 'top';
    const result = await AdMob.showBanner(this.bannerTopOptions)
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    this.isPrepareBanner = true;
  }

  async showBottomBanner() {
    this.bannerPosition = 'bottom';
    const result = await AdMob.showBanner(this.bannerBottomOptions)
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    this.isPrepareBanner = true;
  }



  async hideBanner() {
    const result = await AdMob.hideBanner()
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    const app: HTMLElement = document.querySelector('ion-router-outlet');
    app.style.marginTop = '0px';
    app.style.marginBottom = '0px';
  }

  async resumeBanner() {
    const result = await AdMob.resumeBanner()
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }

    const app: HTMLElement = document.querySelector('ion-router-outlet');
    app.style.marginBottom = this.appMargin + 'px';
  }

  async removeBanner() {
    const result = await AdMob.removeBanner()
      .catch(e => console.log(e));
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
    this.isLoading = true;
    const result = await AdMob.prepareRewardVideoAd(this.rewardOptions)
      .catch(e => console.log(e))
      .finally(() => this.isLoading = false);
    if (result === undefined) {
      return;
    }
  }

  async showReward() {
    this.eventRewardReceived = undefined;
    const result = AdMob.showRewardVideoAd()
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }
    this.isPrepareReward = false;
  }
  /**
   * ==================== /REWARD ====================
   */

  /**
   * ==================== Interstitial ====================
   */
  async prepareInterstitial() {
    this.isLoading = true;
    const result = AdMob.prepareInterstitial(this.interstitialOptions)
      .catch(e => console.log(e))
      .finally(() => this.isLoading = false);
    if (result === undefined) {
      return;
    }
  }


  async showInterstitial() {
    const result = await AdMob.showInterstitial()
      .catch(e => console.log(e));
    if (result === undefined) {
      return;
    }
    this.isPrepareInterstitial = false;
  }

  /**
   * ==================== /Interstitial ====================
   */
}
