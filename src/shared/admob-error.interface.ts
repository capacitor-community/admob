/**
 * For more information
 * https://developers.google.com/android/reference/com/google/android/gms/ads/AdError
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

  /**
   * Gets the cause of this error or null if the cause is nonexistent or unknown.
   */
  cause: string;

  /**
   * Gets the domain of the error.
   * MobileAds.ERROR_DOMAIN for Google Mobile Ads SDK errors, or a domain defined by mediation networks for mediation errors.
   */
  domain: string;
}