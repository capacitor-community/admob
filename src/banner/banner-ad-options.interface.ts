import type { AdOptions } from '../shared';

import type { BannerAdPosition } from './banner-ad-position.enum';
import type { BannerAdSize } from './banner-ad-size.enum';

/**
 * Options for displaying a banner ad.
 *
 * This interface extends AdOptions.
 */
export interface BannerAdOptions extends AdOptions {
  /**
   * The banner size to display.
   *
   * @default ADAPTIVE_BANNER
   * @since 3.0.0
   */
   adSize?: BannerAdSize;

   /**
    * The position where the banner is displayed.
    *
    * @default TOP_CENTER
    * @since 1.1.2
    */
   position?: BannerAdPosition;
}
