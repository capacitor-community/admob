import { BannerAdOptions } from './banner-ad-options.interface';
import type { PluginListenerHandle } from '@capacitor/core';
import { AdMobBannerSize } from './banner-size.interface';
import { AdMobError } from '../shared';

export interface BannerDefinitions {
   /**
   * Show a banner Ad
   *
   * @group Banner
   * @param options AdOptions
   * @since 1.1.2
   */
    showBanner(options: BannerAdOptions): Promise<void>;

    /**
     * Hide the banner, remove it from screen, but can show it later
     *
     * @group Banner
     * @since 1.1.2
     */
    hideBanner(): Promise<void>;
  
    /**
     * Resume the banner, show it after hide
     *
     * @group Banner
     * @since 1.1.2
     */
    resumeBanner(): Promise<void>;
  
    /**
     * Destroy the banner, remove it from screen.
     *
     * @group Banner
     * @since 1.1.2
     */
  removeBanner(): Promise<void>;
  
   /**
   *
   * @group Banner
   * @param eventName bannerViewChangeSize
   * @param listenerFunc
   * @since 3.0.0
   */
    addListener(
      eventName: 'bannerViewChangeSize',
      listenerFunc: (info: AdMobBannerSize) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: request loaded Banner ad
     *
     * @group Banner
     * @param eventName bannerViewDidReceiveAd
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerViewDidReceiveAd',
      listenerFunc: () => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: request failed Banner ad
     *
     * @group Banner
     * @param eventName bannerView:didFailToReceiveAdWithError
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerView:didFailToReceiveAdWithError',
      listenerFunc: (info: AdMobError) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: full-screen banner view will be presented in response to the user clicking on an ad.
     *
     * @group Banner
     * @param eventName bannerViewWillPresentScreen
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerViewWillPresentScreen',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: The full-screen banner view will be dismissed.
     *
     * @group Banner
     * @param eventName bannerViewWillDismissScreen
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerViewWillDismissScreen',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: The full-screen banner view will been dismissed.
     *
     * @group Banner
     * @param eventName bannerViewWillDismissScreen
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerViewWillDismissScreen',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: The full-screen banner view has been dismissed.
     *
     * @group Banner
     * @param eventName bannerViewDidDismissScreen
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'bannerViewDidDismissScreen',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
}