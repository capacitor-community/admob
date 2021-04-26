import {BannerAdOptions, BannerAdPosition, BannerAdSize} from '../../../../../dist/esm/banner';
import {AdOptions} from '../../../../../dist/esm/shared';

export const bannerTopOptions: BannerAdOptions = {
  adId: 'ca-app-pub-3940256099942544/2934735716',
  adSize: BannerAdSize.ADAPTIVE_BANNER,
  position: BannerAdPosition.TOP_CENTER,
  // npa: false,
};

export const bannerBottomOptions: BannerAdOptions = {
  adId: 'ca-app-pub-3940256099942544/2934735716',
  adSize: BannerAdSize.ADAPTIVE_BANNER,
  position: BannerAdPosition.BOTTOM_CENTER,
  npa: true,
};

export const rewardOptions: AdOptions = {
  adId: 'ca-app-pub-3940256099942544/5224354917',
};

export const interstitialOptions: AdOptions = {
  adId: 'ca-app-pub-3940256099942544/1033173712',
};
