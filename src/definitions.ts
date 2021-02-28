import { PluginListenerHandle } from '@capacitor/core';

export interface AdMobPlugin {
  /**
   * Initialize AdMob with AdMobInitializationOptions
   *
   * @group Initialize
   * @param options AdMobInitializationOptions
   * @since 1.1.2
   */
  initialize(options: AdMobInitializationOptions): Promise<void>;

  /**
   * Show a banner Ad
   *
   * @group Banner
   * @param options AdOptions
   * @since 1.1.2
   */
  showBanner(options: AdOptions): Promise<void>;

  /**
   * Hide the banner, remove it from screen, but can show it later
   *
   * @group Banner
   * @since 1.1.2
   */
  hideBanner(): Promise<void>;

  /**
   * Resume the banner, show it after hide
   *
   * @group Banner
   * @since 1.1.2
   */
  resumeBanner(): Promise<void>;

  /**
   * Destroy the banner, remove it from screen.
   *
   * @group Banner
   * @since 1.1.2
   */
  removeBanner(): Promise<void>;

  /**
   * Prepare interstitial banner
   *
   * @group Interstitial
   * @param options AdOptions
   * @since 1.1.2
   */
  prepareInterstitial(options: AdOptions): Promise<void>;

  /**
   * Show interstitial ad when it’s ready
   *
   * @group Interstitial
   * @since 1.1.2
   */
  showInterstitial(): Promise<void>;

  /**
   * Prepare a reward video ad
   *
   * @group RewardVideo
   * @param options AdOptions
   * @since 1.1.2
   */
  prepareRewardVideoAd(options: AdOptions): Promise<void>;

  /**
   * Show a reward video ad
   *
   * @group RewardVideo
   * @since 1.1.2
   */
  showRewardVideoAd(): Promise<AdMobRewardItem>;

  /**
   *
   * @group Banner
   * @param eventName bannerViewReceiveAdSize
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewReceiveAdSize',
    listenerFunc: (info: AdMobBannerSize) => void,
  ): PluginListenerHandle;

  /**
   * Notice: request loaded Banner ad
   *
   * @group Banner
   * @param eventName bannerViewDidReceiveAd
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewDidReceiveAd',
    listenerFunc: () => void,
  ): PluginListenerHandle;

  /**
   * Notice: request failed Banner ad
   *
   * @group Banner
   * @param eventName bannerView:didFailToReceiveAdWithError
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerView:didFailToReceiveAdWithError',
    listenerFunc: (info: AdMobError) => void,
  ): PluginListenerHandle;

  /**
   * Notice: full-screen banner view will be presented in response to the user clicking on an ad.
   *
   * @group Banner
   * @param eventName bannerViewWillPresentScreen
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewWillPresentScreen',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: The full-screen banner view will be dismissed.
   *
   * @group Banner
   * @param eventName bannerViewWillDismissScreen
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewWillDismissScreen',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: The full-screen banner view will been dismissed.
   *
   * @group Banner
   * @param eventName bannerViewWillDismissScreen
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewWillDismissScreen',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: The full-screen banner view has been dismissed.
   *
   * @group Banner
   * @param eventName bannerViewDidDismissScreen
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'bannerViewDidDismissScreen',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: Prepared InterstitialAd
   *
   * @group InterstitialAd
   * @param eventName onInterstitialAdLoaded
   * @param listenerFunc
   * @since 1.1.2
   */
  addListener(
    eventName: 'onInterstitialAdLoaded',
    listenerFunc: () => void,
  ): PluginListenerHandle;

  /**
   * Notice: Prepared RewardedVideo
   *
   * @group RewardedVideo
   * @param eventName onRewardedVideoAdLoaded
   * @param listenerFunc
   * @since 1.1.2
   */
  addListener(
    eventName: 'onRewardedVideoAdLoaded',
    listenerFunc: () => void,
  ): PluginListenerHandle;

  /**
   * Notice: Interstitial ad opened
   *
   * @group fullscreen
   * @param eventName adDidPresentFullScreenContent
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'adDidPresentFullScreenContent',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: Dismiss Content
   *
   * @group fullscreen
   * @param eventName adDidDismissFullScreenContent
   * @param listenerFunc
   * @since 3.0.0
   */
  addListener(
    eventName: 'adDidDismissFullScreenContent',
    listenerFunc: (info: any) => void,
  ): PluginListenerHandle;

  /**
   * Notice: Interstitial ad is be failed to open
   *
   * @group fullscreen
   * @param eventName didFailToPresentFullScreenContentWithError
   * @param listenerFunc
   * @since 1.2.0
   */
  addListener(
    eventName: 'didFailToPresentFullScreenContentWithError',
    listenerFunc: (info: AdMobError) => void,
  ): PluginListenerHandle;
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

export interface AdOptions {
  /**
   * The ad unit ID that you want to request
   *
   * @see https://support.google.com/admob/answer/7356431?hl=en
   * @since 1.1.2
   */
  adId: string;

  /**
   * Banner Ad Size, defaults to SMART_BANNER.
   * IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE,
   * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
   *
   * @default SMART_BANNER
   * @since 1.1.2
   */
  adSize?: AdSize;

  /**
   * Set Banner Ad position.
   * TOP_CENTER or CENTER or BOTTOM_CENTER
   *
   * @default TOP_CENTER
   * @since 1.1.2
   */
  position?: AdPosition;

  /**
   * You can use test mode of ad.
   *
   * @default false
   * @since 1.1.2
   */
  isTesting?: boolean;

  /**
   * Margin Banner. Default is 0px;
   * If position is BOTTOM_CENTER, margin is be margin-bottom.
   * If position is TOP_CENTER, margin is be margin-top.
   *
   * @default 0
   * @since 1.1.2
   */
  margin?: number;

  /**
   * The default behavior of the Google Mobile Ads SDK is to serve personalized ads.
   * Set this to true to request Non-Personalized Ads
   *
   * @see https://developers.google.com/admob/ios/eu-consent
   * @see https://developers.google.com/admob/android/eu-consent
   * @default false
   * @since 1.2.0
   */
  npa?: boolean;
}

/**
 * For more information
 * https://developers.google.com/admob/android/rewarded-video-adapters?hl=en
 */
export interface AdMobRewardItem {
  /**
   * Rewarded type user got
   */
  type: string;

  /**
   * Rewarded amount user got
   */
  amount: number;
}

/**
 * When notice listener of OnAdLoaded, you can get banner size.
 */
export interface AdMobBannerSize {
  width: number
  height: number;
}

/**
 * For more information
 * https://developers.google.com/android/reference/com/google/android/gms/ads/AdError
 */
export interface AdMobError {
  /**
   * Gets the error's code.
   */
  code: number;

  /**
   * Gets the message describing the error.
   */
  message: string;

  /**
   * Gets the cause of this error or null if the cause is nonexistent or unknown.
   */
  cause: string;

  /**
   * Gets the domain of the error.
   * MobileAds.ERROR_DOMAIN for Google Mobile Ads SDK errors, or a domain defined by mediation networks for mediation errors.
   */
  domain: string;
}

/**
 *  For more information:
 *  https://developers.google.com/admob/ios/banner#banner_sizes
 *  https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize
 * */
export enum AdSize {
  /**
   * Mobile Marketing Association (MMA)
   * banner ad size (320x50 density-independent pixels).
   */
  BANNER = 'BANNER',

  /**
   * A dynamically sized banner that matches its parent's
   * width and expands/contracts its height to match the ad's
   * content after loading completes.
   */
  FLUID = 'FLUID',

  /**
   * Interactive Advertising Bureau (IAB)
   * full banner ad size (468x60 density-independent pixels).
   */
  FULL_BANNER = 'FULL_BANNER',

  /**
   * Large banner ad size (320x100 density-independent pixels).
   */
  LARGE_BANNER = 'LARGE_BANNER',

  /**
   * Interactive Advertising Bureau (IAB)
   * leaderboard ad size (728x90 density-independent pixels).
   */
  LEADERBOARD = 'LEADERBOARD',

  /**
   * Interactive Advertising Bureau (IAB)
   * medium rectangle ad size (300x250 density-independent pixels).
   */
  MEDIUM_RECTANGLE = 'MEDIUM_RECTANGLE',

  /**
   * deprecated: A dynamically sized banner that is full-width and auto-height.
   */
  SMART_BANNER = 'SMART_BANNER',

  /**
   * A dynamically sized banner that is full-width and auto-height.
   */
  ADAPTIVE_BANNER = 'ADAPTIVE_BANNER',

  /**
   * To define a custom banner size, set your desired AdSize
   */
  CUSTOM = 'CUSTOM',
}

/**
 * @see https://developer.android.com/reference/android/widget/LinearLayout#attr_android:gravity
 */
export enum AdPosition {
  /**
   * Banner position be top-center
   */
  TOP_CENTER = 'TOP_CENTER',

  /**
   * Banner position be center
   */
  CENTER = 'CENTER',

  /**
   * Banner position be bottom-center(default)
   */
  BOTTOM_CENTER = 'BOTTOM_CENTER',
}
