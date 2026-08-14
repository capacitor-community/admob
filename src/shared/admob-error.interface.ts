/**
 * An error returned by the Google Mobile Ads SDK.
 *
 * @see https://developers.google.com/admob/android/reference/com/google/android/gms/ads/AdError
 */
 export interface AdMobError {
  /**
   * Gets the error's code.
   */
  code: number;

  /**
   * Gets the message describing the error.
   */
  message: string;
}
