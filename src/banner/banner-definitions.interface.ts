import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdMobError, AdMobRevenueData } from '../shared';

import type { BannerAdOptions } from './banner-ad-options.interface';
import type { BannerAdPluginEvents } from './banner-ad-plugin-events.enum';
import type { AdMobBannerSize } from './banner-size.interface';

// This is just to validate that we do not forget to implement any event name
export type BannerDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  BannerAdPluginEvents,
  BannerDefinitions
>;

export interface BannerDefinitions {
  /**
   * Displays a banner ad.
   *
   * @group Banner
   * @param options AdOptions
   * @since 1.1.2
   */
  showBanner(options: BannerAdOptions): Promise<void>;

  /**
   * Hides the current banner without destroying it.
   *
   * @group Banner
   * @since 1.1.2
   */
  hideBanner(): Promise<void>;

  /**
   * Shows a previously hidden banner.
   *
   * @group Banner
   * @since 1.1.2
   */
  resumeBanner(): Promise<void>;

  /**
   * Destroys the current banner and removes it from the screen.
   *
   * @group Banner
   * @since 1.1.2
   */
  removeBanner(): Promise<void>;

  /**
   * Listens for changes to the displayed banner dimensions.
   *
   * @group Banner
   * @param eventName bannerAdSizeChanged
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.SizeChanged,
    listenerFunc: (info: AdMobBannerSize) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner ad load events.
   *
   * @group Banner
   * @param eventName bannerAdLoaded
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.Loaded,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner ad load failures.
   *
   * @group Banner
   * @param eventName bannerAdFailedToLoad
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.FailedToLoad,
    listenerFunc: (info: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner overlay opened events.
   *
   * @group Banner
   * @param eventName bannerAdOpened
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.Opened,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner overlay closed events.
   *
   * @group Banner
   * @param eventName bannerAdClosed
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.Closed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner impression events.
   *
   * @group Banner
   * @param eventName AdImpression
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: BannerAdPluginEvents.AdImpression,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for banner impression-level ad revenue events.
   *
   * @group Banner
   */
  addListener(
    eventName: BannerAdPluginEvents.AdPaid,
    listenerFunc: (data: AdMobRevenueData) => void,
  ): Promise<PluginListenerHandle>;
}
