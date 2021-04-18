package com.getcapacitor.community.admob.rewarded

enum class RewardAdPluginEvents(val webEventName: String) {
    Loaded("onRewardedVideoAdLoaded"),
    FailedToLoad("onRewardedVideoAdFailedToLoad"),
    Showed("onRewardedVideoAdShowed"),
    FailedToShow("onRewardedVideoAdFailedToShow"),
    Dismissed("onRewardedVideoAdDismissed"),
    Rewarded("onRewardedVideoAdReward")
}