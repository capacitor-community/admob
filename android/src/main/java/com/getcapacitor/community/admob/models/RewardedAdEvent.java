package com.getcapacitor.community.admob.models;

import com.getcapacitor.JSObject;

public final class RewardedAdEvent {
    RewardedAdEventName name;
    JSObject payload;

    public RewardedAdEvent(RewardedAdEventName name, JSObject payload) {
        this.name = name;
        this.payload = payload;
    }
}
