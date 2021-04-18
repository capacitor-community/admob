import { AdOptions } from '../shared/ad-options.interface';
import { AdMobRewardItem } from './reward-item.interface';
import type { PluginListenerHandle } from '@capacitor/core';
import { RewardAdPluginEvents } from './reward-ad-plugin-events.enum';
import { ValidateAllEventsEnumAreImplemented} from '../private/validate-all-events-implemented.type'
import { AdMobError } from '../shared';
import { RewardAdLoadInfo } from './reward-ad-load-info.interface';

// This is just to validate that we do not forget to implement any event name
export type RewardDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<RewardAdPluginEvents, RewardDefinitions>;

export interface RewardDefinitions {
    /**
   * Prepare a reward video ad
   *
   * @group RewardVideo
   * @param options AdOptions
   * @since 1.1.2
   */
     prepareRewardVideoAd(options: AdOptions): Promise<RewardAdLoadInfo>;

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
  ): PluginListenerHandle;

  addListener (
    eventName: RewardAdPluginEvents.Loaded,
    listenerFunc: (info: RewardAdLoadInfo ) => void,
  ): PluginListenerHandle;

  addListener (
    eventName: RewardAdPluginEvents.Rewarded,
    listenerFunc: (reward: AdMobRewardItem) => void,
  ): PluginListenerHandle;

  addListener(
    eventName: RewardAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): PluginListenerHandle;

  
  addListener(
    eventName: RewardAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ) : PluginListenerHandle;


  addListener (
    eventName: RewardAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): PluginListenerHandle;
}