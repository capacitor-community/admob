package com.getcapacitor.community.admob.interstitial

import com.getcapacitor.community.admob.models.FullScreenAdEvents

enum class InterstitialAdPluginEvents(val webEventName: String): FullScreenAdEvents {
    Loaded("interstitialAdLoaded"),
    FailedToLoad("interstitialAdFailedToLoad"),
    Showed("interstitialAdShowed"),
    FailedToShow("interstitialAdFailedToShow"),
    Dismissed("interstitialAdDismissed"),
}