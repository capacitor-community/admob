// This enum should be keep in sync with their native equivalents with the same name
export enum RewardAdPluginEvents {
  Loaded= 'onRewardedVideoAdLoaded',
  FailedToLoad = 'onRewardedVideoAdFailedToLoad',
  /**
   * Emits when the AdReward video is visible to the user
   */
  Showed= 'onRewardedVideoAdShowed',
  FailedToShow = 'onRewardedVideoAdFailedToShow',
  /**
   * Emits when the AdReward video is not visible to the user anymore.
   * 
   * **Important**: This has nothing to do with the reward it self. This event
   * will emits in this two cases:
   * 1. The user starts the video ad but close it before the reward emit.
   * 2. The user start the video and see it until end, then gets the reward
   * and after that the ad is closed.
   */
  Dismissed= 'onRewardedVideoAdDismissed',
  Rewarded= 'onRewardedVideoAdReward',
}