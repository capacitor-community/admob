import type { AdOptions } from '../shared';

import type { BannerAdPosition } from './banner-ad-position.enum';
import type { BannerAdSize } from './banner-ad-size.enum';

/**
 * This interface extends AdOptions
 */
export interface BannerAdOptions extends AdOptions {
  /**
   * Banner Ad Size, defaults to ADAPTIVE_BANNER.
   * IT can be: ADAPTIVE_BANNER, SMART_BANNER, BANNER,
   * MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD
   *
   * @default ADAPTIVE_BANNER
   * @since 3.0.0
   */
   adSize?: BannerAdSize;

   /**
    * Set Banner Ad position.
    * TOP_CENTER or CENTER or BOTTOM_CENTER
    *
    * @default TOP_CENTER
    * @since 1.1.2
    */
   position?: BannerAdPosition;
}
