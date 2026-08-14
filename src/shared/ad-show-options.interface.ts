/**
 * Options for selecting a previously loaded ad to show or inspect.
 */
export interface AdShowOptions {
  /**
   * The ad unit ID of a previously prepared ad to target.
   * If omitted, the operation targets the most recently prepared ad.
   *
   * @since 8.0.1
   */
  adId?: string;
}
