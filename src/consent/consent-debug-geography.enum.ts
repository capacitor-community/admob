/**
 *  For more information:
 *  https://developers.google.com/admob/unity/reference/namespace/google-mobile-ads/ump/api#debuggeography
 *
 * */

export enum AdmobConsentDebugGeography {
  /**
   * Debug geography disabled.
   */
  DISABLED = 0,

  /**
   * Geography appears as in EEA for debug devices.
   */
  EEA = 1,

  /**
   * Geography appears as not in EEA for debug devices.
   * @deprecated
   */
  NOT_EEA = 2,

  /**
   * Geography appears as in regulated US state for debug devices.
   */
  US = 3,

  /**
   * Geography appears as OTHER state for debug devices.
   */
  OTHER = 4,
}
