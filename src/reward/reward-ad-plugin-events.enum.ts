// This enum should be keep in sync with their native equivalents with the same name
export enum RewardAdPluginEvents {
  Loaded= 'onRewardedVideoAdLoaded',
  FailedToLoad= 'onRewardedVideoAdFailedToLoad',
  Showed= 'onRewardedVideoAdShowed',
  FailedToShow= 'onRewardedVideoAdFailedToShow',
  Dismissed= 'onRewardedVideoAdDismissed',
  Rewarded= 'onRewardedVideoAdReward',
}