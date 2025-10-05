import type { AdmobConsentStatus } from './consent-status.enum';
import type { PrivacyOptionsRequirementStatus } from './privacy-options-requirement-status.enum';

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

  /**
   * If `true` an ad can be shown.
   * @since 7.0.3
   */
  canRequestAds: boolean;

  /**
   * Privacy options requirement status of the user.
   * @since 7.0.3
   */
  privacyOptionsRequirementStatus: PrivacyOptionsRequirementStatus;
}
