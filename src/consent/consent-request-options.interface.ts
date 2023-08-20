import { AdmobConsentDebugGeography } from './consent-debug-geography.enum';

export interface AdmobConsentRequestOptions {
  /**
   * Sets the debug geography to test the consent locally.
   * @since 5.0.0
   */
  debugGeography?: AdmobConsentDebugGeography;

  /**
   * An array of test device IDs to allow.
   * Note: On iOS, the ID may renew if you uninstall and reinstall the app.
   * @since 5.0.0
   */
  testDeviceIdentifiers?: string[];

  /**
   * Set to `true` to provide the option for the user to accept being shown personalized ads.
   * @default false
   * @since 5.0.0
   */
  tagForUnderAgeOfConsent?: boolean;
}
