// This enum should be keep in sync with their native equivalents with the same name
export enum InterstitialAdPluginEvents {
  /**
   * Emits after trying to prepare and Interstitial, when it is loaded and ready to be show
   */
  Loaded = 'interstitialAdLoaded',
  /**
   * Emits after trying to prepare and Interstitial, when it could not be loaded
   */
  FailedToLoad = 'interstitialAdFailedToLoad',
  /**
   * Emits when the Interstitial ad is visible to the user
   */
  Showed = 'interstitialAdShowed',
  /**
   * Emits when the Interstitial ad is failed to show
   */
  FailedToShow = 'interstitialAdFailedToShow',
  /**
   * Emits when the Interstitial ad is not visible to the user anymore.
   */
  Dismissed= 'interstitialAdDismissed',
}
