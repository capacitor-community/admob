export interface AdOptions {
  /**
   * The ad unit ID that you want to request
   *
   * @see https://support.google.com/admob/answer/7356431?hl=en
   * @since 1.1.2
   */
  adId: string;

  /**
   * You can use test mode of ad.
   *
   * @default false
   * @since 1.1.2
   */
  isTesting?: boolean;

  /**
   * Margin Banner. Default is 0px;
   * If position is BOTTOM_CENTER, margin is be margin-bottom.
   * If position is TOP_CENTER, margin is be margin-top.
   *
   * @default 0
   * @since 1.1.2
   */
  margin?: number;

  /**
   * The default behavior of the Google Mobile Ads SDK is to serve personalized ads.
   * Set this to true to request Non-Personalized Ads
   *
   * @see https://developers.google.com/admob/ios/eu-consent
   * @see https://developers.google.com/admob/android/eu-consent
   * @default false
   * @since 1.2.0
   */
  npa?: boolean;

  /**
   * Sets a flag that controls if this interstitial or reward object will be displayed in immersive mode.
   * Call this method before show.
   * During show, if this flag is on and immersive mode is supported,
   * SYSTEM_UI_FLAG_IMMERSIVE_STICKY &SYSTEM_UI_FLAG_HIDE_NAVIGATION will be turned on for interstitial or reward ad.
   *
   * @see https://developers.google.com/admob/android/reference/com/google/android/gms/ads/interstitial/InterstitialAd#setImmersiveMode(boolean)
   * @see https://developers.google.com/admob/android/reference/com/google/android/gms/ads/rewarded/RewardedAd#setImmersiveMode(boolean)
   * @since 7.0.3
   */
  immersiveMode?: boolean;
}
