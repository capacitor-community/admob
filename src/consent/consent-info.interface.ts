import { ConsentStatus } from './consent-status.enum';

export interface ConsentInfo {
  /**
   * The consent status of the user.
   * @since 5.0.0
   */
  status: ConsentStatus;

  /**
   * If `true` a consent form is available and vice versa.
   * @since 5.0.0
   */
  isConsentFormAvailable?: boolean;
}
