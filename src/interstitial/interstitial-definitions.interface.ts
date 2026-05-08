import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError, AdMobRevenueData, AdOptions } from '../shared';

import type { InterstitialAdPluginEvents } from './interstitial-ad-plugin-events.enum';

// This is just to validate that we do not forget to implement any event name
export type InterstitialDefinitionsHasAllEvents =
  ValidateAllEventsEnumAreImplemented<
    InterstitialAdPluginEvents,
    InterstitialDefinitions
  >;

export interface InterstitialDefinitions {
  /**
   * Prepare interstitial banner
   *
   * @group Interstitial
   * @param options AdOptions
   * @since 1.1.2
   */
  prepareInterstitial(options: AdOptions): Promise<AdLoadInfo>;

  /**
   * Show interstitial ad when it’s ready
   *
   * @group Interstitial
   * @since 1.1.2
   */
  showInterstitial(): Promise<void>;

  addListener(
    eventName: InterstitialAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: InterstitialAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: InterstitialAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: InterstitialAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: InterstitialAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: InterstitialAdPluginEvents.AdImpression,
    listenerFunc: (data: AdMobRevenueData) => void,
  ): Promise<PluginListenerHandle>;
}
