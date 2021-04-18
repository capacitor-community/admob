package com.getcapacitor.community.admob.rewarded;

import android.app.Activity;
import android.content.Context;

import androidx.core.util.Supplier;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.admob.executors.Executor;
import com.getcapacitor.community.admob.rewarded.RewardedAdCallbackAndListeners;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.common.util.BiConsumer;

public class AdRewardExecutor extends Executor {

    public static RewardedAd mRewardedAd;

    public AdRewardExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    @PluginMethod
    public void prepareRewardVideoAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions adOptions = AdOptions.getFactory().createRewardVideoOptions(call);

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                        final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                        RewardedAd.load(contextSupplier.get(), id, adRequest, RewardedAdCallbackAndListeners.INSTANCE.getRewardedAdLoadCallback(call, notifyListenersFunction));
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        mRewardedAd.show(
                                activitySupplier.get(),
                                RewardedAdCallbackAndListeners.INSTANCE.getOnUserEarnedRewardListener(call, notifyListenersFunction)
                        );
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

}
