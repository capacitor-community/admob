package com.getcapacitor.community.admob.executors;

import android.app.Activity;
import android.content.Context;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.RewardedAdEventName;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.util.BiConsumer;

public class AdRewardExecutor extends Executor {
    private RewardedVideoAd mRewardedVideoAd;

    public AdRewardExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    /**
     * Will return a {@link RewardedVideoAdListener} ready to attach to a new created
     * {@link RewardedVideoAd}
     */
    static RewardedVideoAdListener getRewardedVideoAdListener(PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        return new RewardedVideoAdListener() {

            @Override
            public void onRewardedVideoAdLoaded() {
                call.success(new JSObject().put("value", true));
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoAdLoaded.name(), new JSObject().put("value", true));
            }

            @Override
            public void onRewardedVideoAdOpened() {
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoAdOpened.name(), new JSObject().put("value", true));
            }

            @Override
            public void onRewardedVideoStarted() {
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoStarted.name(), new JSObject().put("value", true));
            }

            @Override
            public void onRewardedVideoAdClosed() {
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoAdClosed.name(), new JSObject().put("value", true));
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                final JSObject adMobRewardItem = new JSObject();
                adMobRewardItem.put("type", rewardItem.getType());
                adMobRewardItem.put("amount", rewardItem.getAmount());
                notifyListenersFunction.accept(RewardedAdEventName.onRewarded.name(), adMobRewardItem);
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                notifyListenersFunction.accept(
                    RewardedAdEventName.onRewardedVideoAdLeftApplication.name(),
                    new JSObject().put("value", true)
                );
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                JSObject adMobError = new JSObject();
                adMobError.put("code", errorCode);
                adMobError.put("reason", RequestHelper.getRequestErrorReason(errorCode));
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoAdFailedToLoad.name(), adMobError);
            }

            @Override
            public void onRewardedVideoCompleted() {
                notifyListenersFunction.accept(RewardedAdEventName.onRewardedVideoCompleted.name(), new JSObject().put("value", true));
            }
        };
    }

    public void prepareRewardVideoAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions adOptions = AdOptions.getFactory().createRewardVideoOptions(call);

        try {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(contextSupplier.get());

            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                        final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                        mRewardedVideoAd.loadAd(id, adRequest);
                        mRewardedVideoAd.setRewardedVideoAdListener(getRewardedVideoAdListener(call, notifyListenersFunction));
                    }
                );
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        if (mRewardedVideoAd != null && mRewardedVideoAd.isLoaded()) {
                            mRewardedVideoAd.show();
                            call.success(new JSObject().put("value", true));
                        } else {
                            call.error("The RewardedVideoAd wasn't loaded yet.");
                        }
                    }
                );
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void pauseRewardedVideo(PluginCall call) {
        try {
            activitySupplier.get().runOnUiThread(() -> mRewardedVideoAd.pause(contextSupplier.get()));
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void resumeRewardedVideo(PluginCall call) {
        try {
            activitySupplier.get().runOnUiThread(() -> mRewardedVideoAd.resume(contextSupplier.get()));
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void destroyRewardedVideo(PluginCall call) {
        try {
            activitySupplier.get().runOnUiThread(() -> mRewardedVideoAd.destroy(contextSupplier.get()));
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }
}
