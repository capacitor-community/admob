// This enum should be keep in sync with their native equivalents with the same name
export enum InterstitialAdPluginEvents {
  /**
   * Emits when an interstitial ad has loaded and is ready to show.
   */
  Loaded = 'interstitialAdLoaded',
  /**
   * Emits when an interstitial ad fails to load.
   */
  FailedToLoad = 'interstitialAdFailedToLoad',
  /**
   * Emits when an interstitial ad is shown.
   */
  Showed = 'interstitialAdShowed',
  /**
   * Emits when a loaded interstitial ad fails to show.
   */
  FailedToShow = 'interstitialAdFailedToShow',
  /**
   * Emits when an interstitial ad is dismissed.
   */
  Dismissed= 'interstitialAdDismissed',
  /**
   * Emits impression-level ad revenue data when a paid event is recorded.
   */
  AdImpression = 'interstitialAdImpression',
}
