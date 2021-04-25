package com.getcapacitor.community.admob.rewarded

import com.getcapacitor.community.admob.models.LoadPluginEventNames

object RewardAdPluginEvents: LoadPluginEventNames {
    const val Loaded = "onRewardedVideoAdLoaded"
    const val FailedToLoad = "onRewardedVideoAdFailedToLoad"
    const val Rewarded = "onRewardedVideoAdReward"
    override val Showed = "onRewardedVideoAdShowed"
    override val FailedToShow = "onRewardedVideoAdFailedToShow"
    override val Dismissed = "onRewardedVideoAdDismissed"
}