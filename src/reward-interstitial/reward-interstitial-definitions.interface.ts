import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError, AdMobRevenueData, AdShowOptions } from '../shared';

import type { RewardInterstitialAdOptions } from './reward-interstitial-ad-options.interface';
import type { RewardInterstitialAdPluginEvents } from './reward-interstitial-ad-plugin-events.enum';
import type { AdMobRewardInterstitialItem } from './reward-interstitial-item.interface';

// This is just to validate that we do not forget to implement any event name
export type RewardInterstitialDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  RewardInterstitialAdPluginEvents,
  RewardInterstitialDefinitions
>;

export interface RewardInterstitialDefinitions {
  /**
   * Loads a rewarded interstitial ad and returns the loaded ad unit ID.
   *
   * @group RewardInterstitial
   * @param options RewardInterstitialAdOptions
   * @since 1.1.2
   */
  prepareRewardInterstitialAd(options: RewardInterstitialAdOptions): Promise<AdLoadInfo>;

  /**
   * Shows a loaded rewarded interstitial ad and resolves when the user earns the reward.
   *
   * @group RewardInterstitial
   * @param options Optional. Pass { adId } to show a specific prepared ad instead of the most recent one.
   * @since 1.1.2
   */
  showRewardInterstitialAd(options?: AdShowOptions): Promise<AdMobRewardInterstitialItem>;

  /**
   * Listens for rewarded interstitial ad load failures.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded interstitial ad load events.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for earned reward events.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.Rewarded,
    listenerFunc: (reward: AdMobRewardInterstitialItem) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded interstitial ad dismissed events.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded interstitial ad show failures.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded interstitial ad shown events.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded interstitial impression-level ad revenue events.
   */
  addListener(
    eventName: RewardInterstitialAdPluginEvents.AdImpression,
    listenerFunc: (data: AdMobRevenueData) => void,
  ): Promise<PluginListenerHandle>;
}
