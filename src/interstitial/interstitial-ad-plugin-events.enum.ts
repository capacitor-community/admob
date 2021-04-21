// This enum should be keep in sync with their native equivalents with the same name
export enum InterstitialAdPluginEvents {
  /**
   * Emits after trying to prepare and Interstitial, when it is loaded and ready to be show
   */
  Loaded = 'onRewardedVideoAdLoaded',
  /**
   * Emits after trying to prepare and Interstitial, when it could not be loaded
   */
  FailedToLoad = 'onRewardedVideoAdFailedToLoad',
  /**
   * Emits when the Interstitial ad is visible to the user
   */
  Showed = 'onRewardedVideoAdShowed',
  FailedToShow = 'onRewardedVideoAdFailedToShow',
  /**
   * Emits when the Interstitial ad is not visible to the user anymore.
   */
  Dismissed= 'onRewardedVideoAdDismissed',
}
