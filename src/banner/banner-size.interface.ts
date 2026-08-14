
/**
 * The displayed banner dimensions in logical display units (dp on Android and points on iOS).
 * A hidden, removed, or failed banner can report both dimensions as `0`.
 */
 export interface AdMobBannerSize {
  /**
   * The displayed banner width.
   */
  width: number;

  /**
   * The displayed banner height.
   */
  height: number;
}
