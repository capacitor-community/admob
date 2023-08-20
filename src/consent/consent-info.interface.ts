import { AdmobConsentStatus } from './consent-status.enum';

export interface AdmobConsentInfo {
  /**
   * The consent status of the user.
   * @since 5.0.0
   */
  status: AdmobConsentStatus;

  /**
   * If `true` a consent form is available and vice versa.
   * @since 5.0.0
   */
  isConsentFormAvailable?: boolean;
}
