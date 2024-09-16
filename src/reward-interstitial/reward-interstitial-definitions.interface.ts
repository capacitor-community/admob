import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdLoadInfo, AdMobError } from '../shared';

import type { RewardInterstitialAdOptions } from './reward-interstitial-ad-options.interface';
import type { RewardInterstitialAdPluginEvents } from './reward-interstitial-ad-plugin-events.enum';
import type { AdMobRewardInterstitialItem } from './reward-interstitial-item.interface';

// This is just to validate that we do not forget to implement any event name
export type RewardInterstitialDefinitionsHasAllEvents =
  ValidateAllEventsEnumAreImplemented<
    RewardInterstitialAdPluginEvents,
    RewardInterstitialDefinitions
  >;

export interface RewardInterstitialDefinitions {
  /**
   * Prepare a reward video ad
   *
   * @group RewardVideo
   * @param options RewardAdOptions
   * @since 1.1.2
   */
  prepareRewardInterstitialAd(
    options: RewardInterstitialAdOptions,
  ): Promise<AdLoadInfo>;

  /**
   * Show a reward video ad
   *
   * @group RewardVideo
   * @since 1.1.2
   */
  showRewardInterstitialAd(): Promise<AdMobRewardInterstitialItem>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.Rewarded,
    listenerFunc: (reward: AdMobRewardInterstitialItem) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: RewardInterstitialAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): Promise<PluginListenerHandle>;
}
