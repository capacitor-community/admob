import type { BannerDefinitions } from './banner';
import type { AdmobConsentDefinitions } from './consent';
import type { InterstitialDefinitions } from './interstitial';
import type { RewardDefinitions } from './reward';
import type { RewardInterstitialDefinitions } from './reward-interstitial';
import type { TrackingAuthorizationStatusInterface } from './shared/tracking-authorization-status.interface';

type AdMobDefinitions = BannerDefinitions &
  RewardDefinitions &
  RewardInterstitialDefinitions &
  InterstitialDefinitions &
  AdmobConsentDefinitions;

export interface AdMobPlugin extends AdMobDefinitions {
  /**
   * Initialize AdMob with AdMobInitializationOptions
   *
   * @group Initialize
   * @param options AdMobInitializationOptions
   * @since 1.1.2
   */
  initialize(options?: AdMobInitializationOptions): Promise<void>;

  /**
   * Confirm requestTrackingAuthorization status (iOS >14)
   *
   * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/3547038-trackingauthorizationstatus
   * @since 3.1.0
   */
  trackingAuthorizationStatus(): Promise<TrackingAuthorizationStatusInterface>;

  /**
   * request requestTrackingAuthorization (iOS >14).
   * This is deprecated method. We recommend UMP Consent.
   *
   * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/3547038-trackingauthorizationstatus
   * @since 5.2.0
   */
  requestTrackingAuthorization(): Promise<void>;

  /**
   * Report application mute state to AdMob SDK
   *
   * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/3547038-trackingauthorizationstatus
   * @since 4.1.1
   */
  setApplicationMuted(options: ApplicationMutedOptions): Promise<void>;

  /**
   * Report application volume to AdMob SDK
   *
   * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/3547038-trackingauthorizationstatus
   * @since 4.1.1
   */
  setApplicationVolume(options: ApplicationVolumeOptions): Promise<void>;
}

export interface AdMobInitializationOptions {
  /**
   * An Array of devices IDs that will be marked as tested devices if {@link AdMobInitializationOptions.initializeForTesting} is true
   * (Real Ads will be served to Testing devices, but they will not count as 'real').
   *
   * @see https://developers.google.com/admob/android/test-ads#enable_test_devices
   * @since 1.2.0
   */
  testingDevices?: string[];

  /**
   * If set to true, the devices on {@link AdMobInitializationOptions.testingDevices} will
   * be registered to receive test production ads.
   *
   * @see AdMobInitializationOptions.testingDevices
   * @default false
   * @since 1.2.0
   */
  initializeForTesting?: boolean;

  /**
   * For purposes of the Children's Online Privacy Protection Act (COPPA),
   * there is a setting called tagForChildDirectedTreatment.
   *
   * @see https://developers.google.com/admob/android/targeting#child-directed_setting
   * @since 3.1.0
   */
  tagForChildDirectedTreatment?: boolean;

  /**
   * When using this feature,
   * a Tag For Users under the Age of Consent in Europe (TFUA) parameter will be included in all future ad requests.
   *
   * @see https://developers.google.com/admob/android/targeting#users_under_the_age_of_consent
   * @since 3.1.0
   */
  tagForUnderAgeOfConsent?: boolean;

  /**
   * As an app developer,
   * you can indicate whether you want Google to treat your content as child-directed when you make an ad request.
   *
   * @see https://developers.google.com/admob/android/targeting#child-directed_setting
   * @since 3.1.0
   */
  maxAdContentRating?: MaxAdContentRating;
}

export enum MaxAdContentRating {
  /**
   * Content suitable for general audiences, including families.
   */
  General = 'General',

  /**
   * Content suitable for most audiences with parental guidance.
   */
  ParentalGuidance = 'ParentalGuidance',

  /**
   * Content suitable for teen and older audiences.
   */
  Teen = 'Teen',

  /**
   * Content suitable only for mature audiences.
   */
  MatureAudience = 'MatureAudience',
}

export interface ApplicationMutedOptions {
  /**
   * To inform the SDK that the app volume has been muted.
   * Note: Video ads that are ineligible to be shown with muted audio are not returned for ad requests made,
   * when the app volume is reported as muted or set to a value of 0. This may restrict a subset of the broader video ads pool from serving.
   *
   * @see https://developers.google.com/admob/android/global-settings
   * @since 4.1.1
   */
  muted?: boolean;
}

export interface ApplicationVolumeOptions {
  /**
   * If your app has its own volume controls (such as custom music or sound effect volumes),
   * disclosing app volume to the Google Mobile Ads SDK allows video ads to respect app volume settings.
   * enable set 0.0 - 1.0, any float allowed.
   *
   * @see https://developers.google.com/admob/android/global-settings
   * @since 4.1.1
   */
  volume?: 0.0 | 0.1 | 0.2 | 0.3 | 0.4 | 0.5 | 0.6 | 0.7 | 0.8 | 0.9 | 1.0;
}
