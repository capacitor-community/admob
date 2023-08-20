import type { AdmobConsentInfo } from './consent-info.interface';
import type { AdmobConsentRequestOptions } from './consent-request-options.interface';

export interface AdmobConsentDefinitions {
  /**
   * Request user consent information
   *
   * @group Consent
   * @param options ConsentRequestOptions
   * @since 5.0.0
   */
  requestConsentInfo(
    options?: AdmobConsentRequestOptions,
  ): Promise<AdmobConsentInfo>;

  /**
   * Shows a google user consent form (rendered from your GDPR message config).
   *
   * @group Consent
   * @since 5.0.0
   */
  showConsentForm(): Promise<AdmobConsentInfo>;

  /**
   * Resets the UMP SDK state. Call requestConsentInfo function again to allow user modify their consent
   *
   * @group Consent
   * @since 5.0.0
   */

  resetConsentInfo(): Promise<void>;
}
