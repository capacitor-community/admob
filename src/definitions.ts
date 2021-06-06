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
   * Set AdMob Global Settings in anywhere
   *
   * @group Initialize
   * @param options
   * @since 3.1.0
   */
  globalSettings(options: AdMobGlobalSettings): Promise<void>;
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

export interface AdMobGlobalSettings {
  /**
   * If your app has its own volume controls (such as custom music or sound effect volumes),
   * disclosing app volume to the Google Mobile Ads SDK allows video ads to respect app volume settings.
   * enable set 0.0 - 1.0
   *
   * @see https://developers.google.com/admob/android/global-settings
   * @since 3.1.0
   */
  volume?: number;

  /**
   * To inform the SDK that the app volume has been muted.
   * Note: Video ads that are ineligible to be shown with muted audio are not returned for ad requests made,
   * when the app volume is reported as muted or set to a value of 0. This may restrict a subset of the broader video ads pool from serving.
   *
   * @see https://developers.google.com/admob/android/global-settings
   * @since 3.1.0
   */
  muted?: boolean;
}
