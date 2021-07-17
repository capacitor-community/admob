import type { AdOptions } from '../shared/ad-options.interface';

// This is a type to ensure that IF ssv is provided, at least one of userId or customData is required.
type AtLeastOne<T> = {[K in keyof T]: Pick<T, K>}[keyof T];

// Because only RewardedAds use SSV. This interface is only available for RewardedAds
export interface RewardAdOptions extends AdOptions {
  /**
   * If you have enabled SSV in your AdMob Application. You can provide customData or
   * a userId be passed to your callback to do further processing on.
   * 
   * @see https://support.google.com/admob/answer/9603226?hl=en-GB
   */
  ssv?: AtLeastOne<{
    /**
     * An optional UserId to pass to your SSV callback function.
     */
    userId: string;
    /**
     * An optional custom set of data to pass to your SSV callback function.
     */
    customData: string;
  }>;
}