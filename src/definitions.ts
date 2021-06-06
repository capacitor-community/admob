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
   * Initialize AdMob with AdMobInitializationOptions
   *
   * @group target
   * @param options AdMobInitializationOptions
   * @since 3.1.0
   */
  target(options: AdMobTargetOptions): Promise<void>;
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

export interface AdMobTargetOptions {
  /**
   * GADRequestConfiguration is an object that collects targeting information
   * to be applied globally via the GADMobileAds shared instance.
   *
   * @see https://developers.google.com/admob/android/targeting#requestconfiguration
   * @since 3.1.0
   */
  requestConfiguration?: boolean;

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
   * WAs an app developer,
   * you can indicate whether you want Google to treat your content as child-directed when you make an ad request.
   *
   * @see https://developers.google.com/admob/android/targeting#child-directed_setting
   * @since 3.1.0
   */
  maxAdContentRating?:
    | 'MAX_AD_CONTENT_RATING_G'
    | 'MAX_AD_CONTENT_RATING_PG'
    | 'MAX_AD_CONTENT_RATING_T'
    | 'MAX_AD_CONTENT_RATING_MA';

  /**
   * When requesting an ad, apps may pass the URL of the content they are serving.
   * This enables keyword targeting to match the ad with the content.
   *
   * @see https://developers.google.com/admob/android/targeting#content_url
   * @since 3.1.0
   */
  contentURL?: string;
}
