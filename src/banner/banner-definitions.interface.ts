import { BannerAdOptions } from './banner-ad-options.interface';
import type { PluginListenerHandle } from '@capacitor/core';
import { AdMobBannerSize } from './banner-size.interface';
import { AdMobError } from '../shared';
import { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import { BannerAdPluginEvents } from './banner-ad-plugin-events.enum';

// This is just to validate that we do not forget to implement any event name
export type BannerDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<BannerAdPluginEvents, BannerDefinitions>;

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
   * @param eventName bannerAdSizeChanged
   * @param listenerFunc
   * @since 3.0.0
   */
    addListener(
      eventName: BannerAdPluginEvents.SizeChanged,
      listenerFunc: (info: AdMobBannerSize) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: request loaded Banner ad
     *
     * @group Banner
     * @param eventName bannerAdLoaded
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: BannerAdPluginEvents.Loaded,
      listenerFunc: (info: AdMobBannerSize) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: request failed Banner ad
     *
     * @group Banner
     * @param eventName bannerAdFailedToLoad
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: BannerAdPluginEvents.FailedToLoad,
      listenerFunc: (info: AdMobError) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: full-screen banner view will be presented in response to the user clicking on an ad.
     *
     * @group Banner
     * @param eventName bannerAdOpened
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: BannerAdPluginEvents.Opened,
      listenerFunc: () => void,
    ): PluginListenerHandle;
  
  
    /**
     * Notice: The full-screen banner view will been dismissed.
     *
     * @group Banner
     * @param eventName bannerAdClosed
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: BannerAdPluginEvents.Closed,
      listenerFunc: () => void,
    ): PluginListenerHandle;
  
  
    addListener(
      eventName: BannerAdPluginEvents.Clicked,
      listenerFunc: () => void,
    ): PluginListenerHandle;
  
    addListener(
      eventName: BannerAdPluginEvents.AdImpression,
      listenerFunc: () => void,
    ): PluginListenerHandle;
}