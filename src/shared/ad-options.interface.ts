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
   * For rewarded Ads SSV only.
   * Set this value to send a user identifier to your rewarded Ads SSV call.
   * 
   * @default undefined
   * @since 3.0.1
   */
  userId?: string;

  /**
   * For rewarded ads SSV only.
   * Set this value to send custom data to your rewarded ads SSV call.
   */
  customData?: string;
}