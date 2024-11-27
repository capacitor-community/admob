package com.getcapacitor.community.admob.rewardedinterstitial

import com.getcapacitor.community.admob.models.LoadPluginEventNames

object RewardInterstitialAdPluginEvents: LoadPluginEventNames {
    const val Loaded = "onRewardedInterstitialAdLoaded"
    const val FailedToLoad = "onRewardedInterstitialAdFailedToLoad"
    const val Rewarded = "onRewardedInterstitialAdReward"
    override val Showed = "onRewardedInterstitialAdShowed"
    override val FailedToShow = "onRewardedInterstitialAdFailedToShow"
    override val Dismissed = "onRewardedInterstitialAdDismissed"
}