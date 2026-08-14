import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError, AdMobRevenueData, AdShowOptions } from '../shared';

import type { RewardAdOptions } from './reward-ad-options.interface';
import type { RewardAdPluginEvents } from './reward-ad-plugin-events.enum';
import type { AdMobRewardItem } from './reward-item.interface';

// This is just to validate that we do not forget to implement any event name
export type RewardDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  RewardAdPluginEvents,
  RewardDefinitions
>;

export interface RewardDefinitions {
  /**
   * Loads a rewarded ad and returns the loaded ad unit ID.
   *
   * @group RewardVideo
   * @param options RewardAdOptions
   * @since 1.1.2
   */
  prepareRewardVideoAd(options: RewardAdOptions): Promise<AdLoadInfo>;

  /**
   * Shows a loaded rewarded ad and resolves when the user earns the reward.
   *
   * @group RewardVideo
   * @param options Optional. Pass { adId } to show a specific prepared ad instead of the most recent one.
   * @since 1.1.2
   */
  showRewardVideoAd(options?: AdShowOptions): Promise<AdMobRewardItem>;

  /**
   * Listens for rewarded ad load failures.
   */
  addListener(
    eventName: RewardAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded ad load events.
   */
  addListener(
    eventName: RewardAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for earned reward events.
   */
  addListener(
    eventName: RewardAdPluginEvents.Rewarded,
    listenerFunc: (reward: AdMobRewardItem) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded ad dismissed events.
   */
  addListener(eventName: RewardAdPluginEvents.Dismissed, listenerFunc: () => void): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded ad show failures.
   */
  addListener(
    eventName: RewardAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded ad shown events.
   */
  addListener(
    eventName: RewardAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listens for rewarded impression-level ad revenue events.
   */
  addListener(
    eventName: RewardAdPluginEvents.AdImpression,
    listenerFunc: (data: AdMobRevenueData) => void,
  ): Promise<PluginListenerHandle>;
}
