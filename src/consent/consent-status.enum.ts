/**
 *  For more information:
 *  https://developers.google.com/admob/unity/reference/namespace/google-mobile-ads/ump/api#consentstatus
 *
 * */

export enum AdmobConsentStatus {
  /**
   * User consent not required.
   */
  NOT_REQUIRED = 'NOT_REQUIRED',

  /**
   * User consent already obtained.
   */
  OBTAINED = 'OBTAINED',

  /**
   * User consent required but not yet obtained.
   */
  REQUIRED = 'REQUIRED',

  /**
   * Unknown consent status, AdsConsent.requestInfoUpdate needs to be called to update it.
   */
  UNKNOWN = 'UNKNOWN',
}
