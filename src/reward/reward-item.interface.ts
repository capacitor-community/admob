/**
 * The reward earned by the user after viewing a rewarded ad.
 */
 export interface AdMobRewardItem {
  /**
   * The reward item type configured for the ad unit.
   */
  type: string;

  /**
   * The reward amount earned by the user.
   */
  amount: number;
}
