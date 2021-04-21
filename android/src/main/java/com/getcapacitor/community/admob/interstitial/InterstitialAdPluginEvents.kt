package com.getcapacitor.community.admob.interstitial

enum class InterstitialAdPluginEvents(val webEventName: String) {
    Loaded("interstitialVideoAdLoaded"),
    FailedToLoad("interstitialVideoAdFailedToLoad"),
    Showed("interstitialVideoAdShowed"),
    FailedToShow("interstitialVideoAdFailedToShow"),
    Dismissed("interstitialVideoAdDismissed"),
}