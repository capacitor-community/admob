package com.getcapacitor.community.admob.models;

public enum FullScreenAdEventName {
    onAdLoaded("onAdLoaded"),
    onAdFailedToLoad("onAdFailedToLoad"),
    adDidPresentFullScreenContent("adDidPresentFullScreenContent"),
    didFailToPresentFullScreenContentWithError("didFailToPresentFullScreenContentWithError"),

    /**
     * Follow iOS Event Name
     * https://developers.google.com/admob/ios/api/reference/Protocols/GADFullScreenContentDelegate#-addidpresentfullscreencontent:
     */
    adDidDismissFullScreenContent("adDidDismissFullScreenContent");

    private String event;

    FullScreenAdEventName(String event) {
        this.event = event;
    }
}
