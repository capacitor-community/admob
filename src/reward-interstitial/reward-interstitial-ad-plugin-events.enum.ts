// This enum should be keep in sync with their native equivalents with the same name
export enum RewardInterstitialAdPluginEvents {
  /**
   * Emits when a rewarded interstitial ad has loaded and is ready to show.
   */
  Loaded = 'onRewardedInterstitialAdLoaded',
  /**
   * Emits when a rewarded interstitial ad fails to load.
   */
  FailedToLoad = 'onRewardedInterstitialAdFailedToLoad',
  /**
   * Emits when a rewarded interstitial ad is shown.
   */
  Showed = 'onRewardedInterstitialAdShowed',
  /**
   * Emits when a loaded rewarded interstitial ad fails to show.
   */
  FailedToShow = 'onRewardedInterstitialAdFailedToShow',
  /**
   * Emits when a rewarded interstitial ad is dismissed.
   *
   * This event does not indicate whether the user earned a reward. Listen for
   * `Rewarded` separately before granting the reward.
   */
  Dismissed = 'onRewardedInterstitialAdDismissed',
  /**
   * Emits when the user earns the advertised reward.
   */
  Rewarded = 'onRewardedInterstitialAdReward',
  /**
   * Emits impression-level ad revenue data when a paid event is recorded.
   */
  AdImpression = 'onRewardedInterstitialAdImpression',
}
