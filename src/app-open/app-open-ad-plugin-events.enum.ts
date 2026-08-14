export enum AppOpenAdPluginEvents {
  /**
   * Emits when an App Open ad has loaded.
   */
  Loaded = 'appOpenAdLoaded',

  /**
   * Emits when an App Open ad fails to load.
   */
  FailedToLoad = 'appOpenAdFailedToLoad',

  /**
   * Emits when an App Open ad is shown.
   */
  Opened = 'appOpenAdOpened',

  /**
   * Emits when an App Open ad is dismissed.
   */
  Closed = 'appOpenAdClosed',

  /**
   * Emits when a loaded App Open ad fails to show.
   */
  FailedToShow = 'appOpenAdFailedToShow',

  /**
   * Emits impression-level ad revenue data when a paid event is recorded.
   */
  AdImpression = 'appOpenAdImpression',
}
