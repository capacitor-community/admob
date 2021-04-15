import { AdOptions } from '../shared/ad-options.interface';
import { BannerAdPosition } from './banner-ad-position.enum';
import { BannerAdSize } from './banner-ad-size.enum';

export interface BannerAdOptions extends AdOptions {
  /**
   * Banner Ad Size, defaults to SMART_BANNER.
   * IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE,
   * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
   *
   * @default SMART_BANNER
   * @since 1.1.2
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