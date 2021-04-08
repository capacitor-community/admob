package com.getcapacitor.community.admob.models;

public enum RewardedAdEventName {
    onAdLoaded("onAdLoaded"),
    onAdFailedToLoad("onAdFailedToLoad");

    private String event;

    RewardedAdEventName(String event) {
        this.event = event;
    }
}
