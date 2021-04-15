import { AdOptions } from '../shared/ad-options.interface';
import { AdMobRewardItem } from './reward-item.interface';

export interface RewardDefinitions {
    /**
   * Prepare a reward video ad
   *
   * @group RewardVideo
   * @param options AdOptions
   * @since 1.1.2
   */
     prepareRewardVideoAd(options: AdOptions): Promise<void>;

     /**
      * Show a reward video ad
      *
      * @group RewardVideo
      * @since 1.1.2
      */
     showRewardVideoAd(): Promise<AdMobRewardItem>;
}