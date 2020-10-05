package com.getcapacitor.community.admob.models;

public enum RewardedAdEventName {
    onRewardedVideoAdLoaded("onRewardedVideoAdLoaded"),
    onRewardedVideoAdOpened("onRewardedVideoAdOpened"),
    onRewardedVideoStarted("onRewardedVideoStarted"),
    onRewardedVideoAdClosed("onRewardedVideoAdClosed"),
    onRewarded("onRewarded"),
    onRewardedVideoAdLeftApplication("onRewardedVideoAdLeftApplication"),
    onRewardedVideoAdFailedToLoad("onRewardedVideoAdLeftApplication"),
    onRewardedVideoCompleted("onRewardedVideoAdFailedToLoad");

    private String event;

    RewardedAdEventName(String event) {
        this.event = event;
    }
}
