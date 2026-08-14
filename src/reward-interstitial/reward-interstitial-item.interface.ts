/**
 * The reward earned by the user after viewing a rewarded interstitial ad.
 */
export interface AdMobRewardInterstitialItem {
  /**
   * The reward item type configured for the ad unit.
   */
  type: string;

  /**
   * The reward amount earned by the user.
   */
  amount: number;
}
