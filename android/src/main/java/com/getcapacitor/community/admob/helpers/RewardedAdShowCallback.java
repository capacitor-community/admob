package com.getcapacitor.community.admob.helpers;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.LoadPluginEventNames;
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError;
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback;
import java.util.function.BiConsumer;

/** Owns a single show call until reward, cancellation, or presentation failure. SDK callbacks run on the UI thread. */
public final class RewardedAdShowCallback
    implements RewardedAdEventCallback, RewardedInterstitialAdEventCallback, OnUserEarnedRewardListener
{

    private PluginCall pendingCall;
    private final LoadPluginEventNames events;
    private final String rewardedEvent;
    private final BiConsumer<String, JSObject> notifyListeners;
    private final Runnable onCompleted;
    private boolean completed;
    private com.google.android.libraries.ads.mobile.sdk.common.AdEventCallback paidCallback;

    public void setPaidCallback(com.google.android.libraries.ads.mobile.sdk.common.AdEventCallback callback) {
        paidCallback = callback;
    }

    @Override
    public void onAdPaid(com.google.android.libraries.ads.mobile.sdk.common.AdValue value) {
        if (!completed && paidCallback != null) paidCallback.onAdPaid(value);
    }

    public RewardedAdShowCallback(
        PluginCall call,
        LoadPluginEventNames events,
        String rewardedEvent,
        BiConsumer<String, JSObject> notifyListeners,
        Runnable onCompleted
    ) {
        this.pendingCall = call;
        this.events = events;
        this.rewardedEvent = rewardedEvent;
        this.notifyListeners = notifyListeners;
        this.onCompleted = onCompleted;
    }

    @Override
    public void onUserEarnedReward(RewardItem item) {
        if (completed || pendingCall == null) return;
        PluginCall call = pendingCall;
        pendingCall = null;
        JSObject reward = new JSObject().put("type", item.getType()).put("amount", item.getAmount());
        notifyListeners.accept(rewardedEvent, reward);
        call.resolve(reward);
    }

    @Override
    public void onAdClicked() {
        if (!completed && events.getAdClicked() != null) notifyListeners.accept(events.getAdClicked(), new JSObject());
    }

    @Override
    public void onAdShowedFullScreenContent() {
        if (!completed) notifyListeners.accept(events.getShowed(), new JSObject());
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        if (!complete()) return;
        reject("Rewarded ad dismissed before earning a reward.", "REWARDED_AD_CANCELLED");
        notifyListeners.accept(events.getDismissed(), new JSObject());
    }

    @Override
    public void onAdFailedToShowFullScreenContent(FullScreenContentError error) {
        if (!complete()) return;
        reject(error.getMessage(), Integer.toString(error.getCode().getValue()));
        notifyListeners.accept(events.getFailedToShow(), new AdMobPluginError(error));
    }

    public void fail(Exception error) {
        if (!complete()) return;
        String message = error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Unable to show rewarded ad.";
        reject(message, "REWARDED_AD_SHOW_FAILED");
        notifyListeners.accept(events.getFailedToShow(), new AdMobPluginError(-1, message));
    }

    private boolean complete() {
        if (completed) return false;
        completed = true;
        onCompleted.run();
        return true;
    }

    private void reject(String message, String code) {
        PluginCall call = pendingCall;
        pendingCall = null;
        if (call != null) call.reject(message, code);
    }
}
