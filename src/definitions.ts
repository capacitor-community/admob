import { PluginListenerHandle } from '@capacitor/core';

declare global {
  interface PluginRegistry {
    AdMob?: AdMobPlugin;
  }
}

export interface AdMobPlugin {
  // Initialize AdMob with appId
  initialize(options: AdMobInitializationOptions): Promise<{ value: boolean }>;

  // Show a banner Ad
  showBanner(options: AdOptions): Promise<{ value: boolean }>;

  // Hide the banner, remove it from screen, but can show it later
  hideBanner(): Promise<{ value: boolean }>;

  // Resume the banner, show it after hide
  resumeBanner(): Promise<{ value: boolean }>;

  // Destroy the banner, remove it from screen.
  removeBanner(): Promise<{ value: boolean }>;

  // Show banner at position
  //showBanner(position: AdPosition): Promise<boolean>;

  // showBannerAtXY(x, y)
  //showBannerAtXY(x: number, y: number): Promise<boolean>;

  // Prepare interstitial banner
  prepareInterstitial(options: AdOptions): Promise<{ value: boolean }>;

  // Show interstitial ad when it’s ready
  showInterstitial(): Promise<{ value: boolean }>;

  // Prepare a reward video ad
  prepareRewardVideoAd(options: AdOptions): Promise<{ value: boolean }>;

  // Show a reward video ad
  showRewardVideoAd(): Promise<{ value: boolean }>;

  // Pause RewardedVideo
  pauseRewardedVideo(): Promise<{ value: boolean }>;

  // Resume RewardedVideo
  resumeRewardedVideo(): Promise<{ value: boolean }>;

  // Close RewardedVideo
  stopRewardedVideo(): Promise<{ value: boolean }>;

  // Sets the values for configuration and targeting
  // setOptions(options: AdOptions): Promise<void>;

  // Get user ad settings
  // getAdSettings(): Promise<any>;

  // AdMob listeners
  addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void
  ): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void ): PluginListenerHandle;

  // Admob RewardVideo listeners
  addListener(eventName: 'onRewardedVideoStarted', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoAdClosed', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'onRewarded', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoAdLeftApplication', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoAdFailedToLoad', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'onRewardedVideoCompleted', listenerFunc: (info: any) => void ): PluginListenerHandle;

  // iOS
  addListener(eventName: 'adViewDidReceiveAd', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'adView:didFailToReceiveAdWithError', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'adViewWillPresentScreen',listenerFunc: (info: any) => void): PluginListenerHandle;

  addListener(eventName: 'adViewWillDismissScreen', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'adViewDidDismissScreen', listenerFunc: (info: any) => void ): PluginListenerHandle;

  addListener(eventName: 'adViewWillLeaveApplication', listenerFunc: (info: any) => void ): PluginListenerHandle;
}

export interface AdMobInitializationOptions {
  requestTrackingAuthorization?: boolean;
  /**
   * An Array of devices IDs that will be marked as tested devices
   * (Real Ads will be served to Testing devices but they will not count as 'real').
   * @see https://developers.google.com/admob/android/test-ads#enable_test_devices
   */
  testingDevices?: string[];
}

export interface AdOptions {
  /**
   * The ad unit ID that you want to request
   * @see https://support.google.com/admob/answer/7356431?hl=en
   */
  adId: string;
  /**
   * Banner Ad Size, defaults to SMART_BANNER.
   * IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE,
   * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
   */
  adSize?: AdSize;
  position?: AdPosition;
  isTesting?: boolean;

  /**
   * Margin Banner. Default is 0 px;
   * If position is BOTTOM_CENTER, margin is be margin-bottom.
   * If position is TOP_CENTER, margin is be margin-top.
   */
  margin?: number;

  /**
   * The default behavior of the Google Mobile Ads SDK is to serve personalized ads.
   * Set this to true to request Non-Personalized Ads
   * @see https://developers.google.com/admob/android/eu-consent
   * @default false
   */
  npa?: boolean;
}

/**
 *  For more information:
 *  https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize
 * */
export enum AdSize {
  // Mobile Marketing Association (MMA)
  // banner ad size (320x50 density-independent pixels).
  BANNER = 'BANNER',

  // A dynamically sized banner that matches its parent's
  // width and expands/contracts its height to match the ad's
  // content after loading completes.
  FLUID = 'FLUID',

  //Interactive Advertising Bureau (IAB)
  // full banner ad size (468x60 density-independent pixels).
  FULL_BANNER = 'FULL_BANNER',

  // Large banner ad size (320x100 density-independent pixels).
  LARGE_BANNER = 'LARGE_BANNER',

  // Interactive Advertising Bureau (IAB)
  // leaderboard ad size (728x90 density-independent pixels).
  LEADERBOARD = 'LEADERBOARD',

  // Interactive Advertising Bureau (IAB)
  // medium rectangle ad size (300x250 density-independent pixels).
  MEDIUM_RECTANGLE = 'MEDIUM_RECTANGLE',

  // A dynamically sized banner that is full-width and auto-height.
  SMART_BANNER = 'SMART_BANNER',

  // To define a custom banner size, set your desired AdSize
  CUSTOM = 'CUSTOM',
}

/**
 *
 * More information
 * https://developer.android.com/reference/android/widget/LinearLayout#attr_android:gravity
 * */

export enum AdPosition {
  TOP_CENTER = 'TOP_CENTER',
  CENTER = 'CENTER',
  BOTTOM_CENTER = 'BOTTOM_CENTER',
}
