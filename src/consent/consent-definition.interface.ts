import type { ConsentInfo } from './consent-info.interface';
import type { ConsentRequestOptions } from './consent-request-options.interface';

export interface ConsentDefinitions {
  /**
   * Request user consent information
   *
   * @group Consent
   * @param options ConsentRequestOptions
   * @since 5.0.0
   */
  requestConsentInfo(options?: ConsentRequestOptions): Promise<ConsentInfo>;

  /**
   * Shows a google user consent form (rendered from your GDPR message config).
   *
   * @group Consent
   * @since 5.0.0
   */
  showConsentForm(): Promise<ConsentInfo>;

  /**
   * Resets the UMP SDK state. Call requestConsentInfo function again to allow user modify their consent
   *
   * @group Consent
   * @since 5.0.0
   */

  resetConsentInfo(): Promise<void>;
}
