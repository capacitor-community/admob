import type { AdOptions } from '../shared/ad-options.interface';

// This is a type to ensure that IF ssv is provided, at least one of userId or customData is required.
type AtLeastOne<T> = {[K in keyof T]: Pick<T, K>}[keyof T];

/**
 * Options for loading a rewarded ad.
 */
export interface RewardAdOptions extends AdOptions {
  /**
   * Server-side verification options for the rewarded ad.
   * Provide at least one of `userId` or `customData`.
   * 
   * @see https://support.google.com/admob/answer/9603226?hl=en-GB
   */
  ssv?: AtLeastOne<{
    /**
     * A user identifier passed to the SSV callback.
     */
    userId: string;
    /**
     * Custom data passed to the SSV callback.
     */
    customData: string;
  }>;
}
