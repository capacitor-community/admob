// This enum should be keep in sync with their native equivalents with the same name
export enum RewardAdPluginEvents {
  /**
   * Emits when a rewarded ad has loaded and is ready to show.
   */
  Loaded = 'onRewardedVideoAdLoaded',
  /**
   * Emits when a rewarded ad fails to load.
   */
  FailedToLoad = 'onRewardedVideoAdFailedToLoad',
  /**
   * Emits when a rewarded ad is shown.
   */
  Showed = 'onRewardedVideoAdShowed',
  /**
   * Emits when a loaded rewarded ad fails to show.
   */
  FailedToShow = 'onRewardedVideoAdFailedToShow',
  /**
   * Emits when a rewarded ad is dismissed.
   *
   * This event does not indicate whether the user earned a reward. Listen for
   * `Rewarded` separately before granting the reward.
   */
  Dismissed= 'onRewardedVideoAdDismissed',
  /**
   * Emits when the user earns the advertised reward.
   */
  Rewarded= 'onRewardedVideoAdReward',
  /**
   * Emits impression-level ad revenue data when a paid event is recorded.
   */
  AdImpression = 'onRewardedVideoAdImpression',
}
