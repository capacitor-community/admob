package com.getcapacitor.community.admob.nativeads;

public enum NativeAdPluginEvents {
    LOADED("nativeAdLoaded"),
    FAILED_TO_LOAD("nativeAdFailedToLoad"),
    CLICKED("nativeAdClicked"),
    IMPRESSION("nativeAdImpression"),
    OPENED("nativeAdOpened"),
    CLOSED("nativeAdClosed"),
    AD_PAID("nativeAdPaid");

    private final String webEventName;

    NativeAdPluginEvents(String webEventName) {
        this.webEventName = webEventName;
    }

    public String getWebEventName() {
        return webEventName;
    }
}
