import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError } from '../shared';

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
   * Prepare a reward video ad
   *
   * @group RewardVideo
   * @param options RewardAdOptions
   * @since 1.1.2
   */
  prepareRewardVideoAd(options: RewardAdOptions): Promise<AdLoadInfo>;

  /**
   * Show a reward video ad
   *
   * @group RewardVideo
   * @since 1.1.2
   */
  showRewardVideoAd(): Promise<AdMobRewardItem>;

  addListener(
    eventName: RewardAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardAdPluginEvents.Rewarded,
    listenerFunc: (reward: AdMobRewardItem) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;
}
