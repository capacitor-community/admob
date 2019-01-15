declare global {
  interface PluginRegistry {
    AdMob?: AdMobPlugin;
  }
}

export interface AdMobPlugin {

  // Initialize AdMob with appId
  initialize(options: { appId: string}): Promise<{value: boolean}>

  // Plugin test method
  echo(options: { value: string }): Promise<{value: string}>;

  // Create a banner
  createBanner(options: AdOptions): Promise<{value: boolean}>;

  // Show banner at position
  //showBanner(position: AdPosition): Promise<boolean>;

  // showBannerAtXY(x, y)
  //showBannerAtXY(x: number, y: number): Promise<boolean>;

  // Hide the banner, remove it from screen, but can show it later
  hideBanner(): Promise<{value: boolean}>;

  // Resume the banner, show it after hide
  resumeBanner(): Promise<{value: boolean}>;

  // Destroy the banner, remove it from screen.
  removeBanner(): Promise<{value: boolean}>;



  // Prepare interstitial banner
  // prepareInterstitial(adOptions: AdOptions): Promise<boolean>;

  // Show interstitial ad when it’s ready
  // showInterstitial(): Promise<boolean>;



    // Prepare a reward video ad
  // prepareRewardVideoAd(adOptions: AdOptions): Promise<boolean>;

  // Show a reward video ad
  // showRewardVideoAd(): Promise<boolean>;

  // Sets the values for configuration and targeting
  // setOptions(options: AdOptions): Promise<void>;



    // Get user ad settings
  // getAdSettings(): Promise<any>;



  // Triggered when failed to receive Ad
  // onAdFailLoad(): Promise<any>

  // Triggered when Ad received
  // onAdLoaded(): Promise<boolean>;

  // Triggered when Ad will be showed on screen
  // onAdPresent(): Promise<boolean>;

  // Triggered when user click the Ad, and will jump out of your App
  // onAdLeaveApp(): Promise<any>;

  // Triggered when dismiss the Ad and back to your App
  // onAdDismiss(): Promise<boolean>;

}



export interface AdOptions {
  adId: string;       // Banner ad ID (required)
  /*
  *
  * Banner Ad Size, defaults to SMART_BANNER.
  * IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE,
  * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
  *
  */
  adSize?: AdSize;
  width?: number;
  height?: number;
  overlap?: boolean;
  position?: AdPosition;
  x?: number;
  y?: number;
  isTesting?: boolean;
  autoShow?: boolean
  orientationRenew?: boolean;
  adExtras?: AdExtras;
  offsetTopBar?: boolean;
}


/*
*  For more information
*   Read:  https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize
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

  // A special variant of FLUID to be set on SearchAdView when
  // loading a DynamicHeightSearchAdRequest.
  SEARCH = 'SEARCH',

  // A dynamically sized banner that is full-width and auto-height.
  SMART_BANNER = 'SMART_BANNER',

  // IAB wide skyscraper ad size (160x600 density-independent pixels).
  // This size is currently not supported by the Google Mobile Ads network;
  // this is intended for mediation ad networks only.
  WIDE_SKYSCRAPER = 'WIDE_SKYSCRAPER',


  // To define a custom banner size, set your desired AdSize
  CUSTOM = 'CUSTOM'

}


/*
*
* More information
* https://developer.android.com/reference/android/widget/LinearLayout#attr_android:gravity
* */

export enum  AdPosition {
  TOP_CENTER,
  CENTER,
  BOTTOM_CENTER,
}


export interface AdExtras {
  color_bg?: string;
  color_bg_top?: string;
  color_border?: string;
  color_link?: string;
  color_text?: string;
  color_url?: string;
}