// This enum should be keep in sync with their native equivalents with the same name
export enum BannerAdPluginEvents {
  /**
   * Emits when the displayed banner size changes.
   */
  SizeChanged = "bannerAdSizeChanged",

  /**
   * Emits when a banner ad has loaded.
   */
  Loaded = "bannerAdLoaded",

  /**
   * Emits when a banner ad fails to load.
   */
  FailedToLoad = "bannerAdFailedToLoad",

  /**
   * Emits when a banner opens an overlay after the user taps it.
   */
  Opened = "bannerAdOpened",

  /**
   * Emits when the banner overlay is closed.
   */
  Closed = "bannerAdClosed",

  /**
   * Emits when an impression is recorded for the banner ad.
   */
  AdImpression = "bannerAdImpression",

  /**
   * Emits impression-level ad revenue data when a paid event is recorded.
   */
  AdPaid = "bannerAdPaid",
}
