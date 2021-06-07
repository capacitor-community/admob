import type { BannerDefinitions } from './banner';
import type { InterstitialDefinitions } from './interstitial';
import type { RewardDefinitions } from './reward';

type AdMobDefinitions = BannerDefinitions &
  RewardDefinitions &
  InterstitialDefinitions;

export interface AdMobPlugin extends AdMobDefinitions {
  /**
   * Initialize AdMob with AdMobInitializationOptions
   *
   * @group Initialize
   * @param options AdMobInitializationOptions
   * @since 1.1.2
   */
  initialize(options: AdMobInitializationOptions): Promise<void>;

  /**
   * targetSettings with AdMobTargetSettings
   *
   * @group target
   * @param options AdMobTargetSettings
   * @since 3.1.0
   */
  targetSettings(options: AdMobTargetSettings): Promise<void>;
}

export interface AdMobInitializationOptions {
  /**
   * Use or not requestTrackingAuthorization in iOS(>14)
   *
   * @see https://developer.apple.com/documentation/apptrackingtransparency/attrackingmanager/3547037-requesttrackingauthorization?changes=latest_minor
   * @since 1.1.2
   */
  requestTrackingAuthorization?: boolean;

  /**
   * An Array of devices IDs that will be marked as tested devices if {@link AdMobInitializationOptions.initializeForTesting} is true
   * (Real Ads will be served to Testing devices but they will not count as 'real').
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
}

export interface AdMobTargetSettings {
  /**
   * For purposes of the Children's Online Privacy Protection Act (COPPA),
   * there is a setting called tagForChildDirectedTreatment.
   *
   * @see https://developers.google.com/admob/android/targeting#child-directed_setting
   * @since 3.1.0
   */
  tagForChildDirectedTreatment?: TargetTag;

  /**
   * When using this feature,
   * a Tag For Users under the Age of Consent in Europe (TFUA) parameter will be included in all future ad requests.
   *
   * @see https://developers.google.com/admob/android/targeting#users_under_the_age_of_consent
   * @since 3.1.0
   */
  tagForUnderAgeOfConsent?: TargetTag;

  /**
   * WAs an app developer,
   * you can indicate whether you want Google to treat your content as child-directed when you make an ad request.
   *
   * @see https://developers.google.com/admob/android/targeting#child-directed_setting
   * @since 3.1.0
   */
  maxAdContentRating?: TargetMaxAdContentRating;
}

export enum TargetTag {
  'TRUE' = 'TRUE',
  'FALSE' = 'FALSE',
  'UNSPECIFIED' = 'UNSPECIFIED',
}

export enum TargetMaxAdContentRating {
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
