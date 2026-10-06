public enum RewardAdPluginEvents: String {
    case adClicked = "onRewardedVideoAdClicked"
    case Loaded = "onRewardedVideoAdLoaded"
    case FailedToLoad = "onRewardedVideoAdFailedToLoad"
    case Showed = "onRewardedVideoAdShowed"
    case FailedToShow = "onRewardedVideoAdFailedToShow"
    case Dismissed = "onRewardedVideoAdDismissed"
    case Rewarded = "onRewardedVideoAdReward"
    case AdImpression = "onRewardedVideoAdImpression"
}
