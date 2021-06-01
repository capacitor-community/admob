/**
 *  For more information:
 *  https://developers.google.com/admob/ios/banner#banner_sizes
 *  https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize
 * 
 * */
 export enum BannerAdSize {
  /**
   * Mobile Marketing Association (MMA)
   * banner ad size (320x50 density-independent pixels).
   */
  BANNER = 'BANNER',

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
   * medium rectangle ad size (300x250 density-independent pixels).
   */
   MEDIUM_RECTANGLE = 'MEDIUM_RECTANGLE',

  /**
   * Interactive Advertising Bureau (IAB)
   * leaderboard ad size (728x90 density-independent pixels).
   */
  LEADERBOARD = 'LEADERBOARD',

  /**
   * A dynamically sized banner that is full-width and auto-height.
   */
   ADAPTIVE_BANNER = 'ADAPTIVE_BANNER',
  
   /**
    * @deprecated 
    * Will be removed in next AdMob versions use `ADAPTIVE_BANNER`
    * Screen width x 32|50|90
    */
   SMART_BANNER = 'SMART_BANNER',

}
