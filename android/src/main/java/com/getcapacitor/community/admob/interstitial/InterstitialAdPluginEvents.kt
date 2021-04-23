package com.getcapacitor.community.admob.interstitial

enum class InterstitialAdPluginEvents(val webEventName: String) {
    Loaded("interstitialAdLoaded"),
    FailedToLoad("interstitialAdFailedToLoad"),
    Showed("interstitialAdShowed"),
    FailedToShow("interstitialAdFailedToShow"),
    Dismissed("interstitialAdDismissed"),
}