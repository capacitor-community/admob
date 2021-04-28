public enum RewardAdPluginEvents: String {
    case Loaded = "onRewardedVideoAdLoaded"
    case FailedToLoad = "onRewardedVideoAdFailedToLoad"
    case Showed = "onRewardedVideoAdShowed"
    case FailedToShow = "onRewardedVideoAdFailedToShow"
    case Dismissed = "onRewardedVideoAdDismissed"
    case Rewarded = "onRewardedVideoAdReward"
}
