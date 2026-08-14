import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError, AdMobRevenueData, AdOptions, AdShowOptions } from '../shared';

import type { InterstitialAdPluginEvents } from './interstitial-ad-plugin-events.enum';

// This is just to validate that we do not forget to implement any event name
export type InterstitialDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  InterstitialAdPluginEvents,
  InterstitialDefinitions
>;

export interface InterstitialDefinitions {
  /**
   * Loads an interstitial ad and returns the loaded ad unit ID.
   *
   * @group Interstitial
   * @param options AdOptions
   * @since 1.1.2
   */
  prepareInterstitial(options: AdOptions): Promise<AdLoadInfo>;

  /**
   * Shows a loaded interstitial ad.
   *
   * @group Interstitial
   * @param options Optional. Pass { adId } to show a specific prepared ad instead of the most recent one.
   * @since 1.1.2
   */
  showInterstitial(options?: AdShowOptions): Promise<void>;

  /**
   * Listens for interstitial ad load failures.
   */
  addListener(
    eventName: InterstitialAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for interstitial ad load events.
   */
  addListener(
    eventName: InterstitialAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for interstitial ad dismissed events.
   */
  addListener(eventName: InterstitialAdPluginEvents.Dismissed, listenerFunc: () => void): Promise<PluginListenerHandle>;

  /**
   * Listens for interstitial ad show failures.
   */
  addListener(
    eventName: InterstitialAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for interstitial ad shown events.
   */
  addListener(
    eventName: InterstitialAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for interstitial impression-level ad revenue events.
   */
  addListener(
    eventName: InterstitialAdPluginEvents.AdImpression,
    listenerFunc: (data: AdMobRevenueData) => void,
  ): Promise<PluginListenerHandle>;
}
