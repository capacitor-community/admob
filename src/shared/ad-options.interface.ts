/**
 * Common options for requesting an ad.
 */
export interface AdOptions {
  /**
   * The ad unit ID to load.
   *
   * @see https://support.google.com/admob/answer/7356431?hl=en
   * @since 1.1.2
   */
  adId: string;

  /**
   * Whether to request a test ad.
   *
   * @default false
   * @since 1.1.2
   */
  isTesting?: boolean;

  /**
   * The banner margin in logical display units (dp on Android and points on iOS).
   * For `BOTTOM_CENTER`, this is the bottom margin. For `TOP_CENTER`, this is the top margin.
   *
   * @default 0
   * @since 1.1.2
   */
  margin?: number;

  /**
   * Whether to request non-personalized ads.
   *
   * @see https://developers.google.com/admob/ios/eu-consent
   * @see https://developers.google.com/admob/android/eu-consent
   * @default false
   * @since 1.2.0
   */
  npa?: boolean;

  /**
   * Whether to display a full-screen ad in immersive mode on Android.
   *
   * @see https://developers.google.com/admob/android/reference/com/google/android/gms/ads/interstitial/InterstitialAd#setImmersiveMode(boolean)
   * @see https://developers.google.com/admob/android/reference/com/google/android/gms/ads/rewarded/RewardedAd#setImmersiveMode(boolean)
   * @since 7.0.3
   */
  immersiveMode?: boolean;
}
