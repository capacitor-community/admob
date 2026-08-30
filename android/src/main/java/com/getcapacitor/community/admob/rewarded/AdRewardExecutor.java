package com.getcapacitor.community.admob.rewarded;

import android.app.Activity;
import android.content.Context;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AdRewardExecutor extends Executor {

    public static final Map<String, RewardedAd> preparedAds = new LinkedHashMap<>();
    public static String lastPreparedAdId;

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

        activitySupplier
            .get()
            .runOnUiThread(() -> {
                try {
                    final String id = AdViewIdHelper.getFinalAdId(adOptions, logTag, contextSupplier.get());
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions, id);
                    RewardedAd.load(
                        adRequest,
                        RewardedAdCallbackAndListeners.INSTANCE.getRewardedAdLoadCallback(call, notifyListenersFunction, adOptions, id)
                    );
                } catch (Exception ex) {
                    call.reject(ex.getLocalizedMessage(), ex);
                }
            });
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        String requestedAdId = call.getString("adId");
        String adId = requestedAdId != null ? requestedAdId : lastPreparedAdId;
        RewardedAd ad = adId != null ? preparedAds.get(adId) : null;

        if (ad == null) {
            String errorMessage = "No Reward Video Ad can be shown. It was not prepared or maybe it failed to be prepared.";
            call.reject(errorMessage);
            AdMobPluginError errorObject = new AdMobPluginError(-1, errorMessage);
            notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad, errorObject);
            return;
        }

        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    ad.setAdEventCallback(
                        RewardedAdCallbackAndListeners.INSTANCE.getRewardedAdEventCallback(notifyListenersFunction, () -> {
                            preparedAds.remove(adId);
                            if (adId != null && adId.equals(lastPreparedAdId)) {
                                lastPreparedAdId = null;
                                for (String remainingAdId : preparedAds.keySet()) lastPreparedAdId = remainingAdId;
                            }
                        })
                    );
                    ad.show(
                        activitySupplier.get(),
                        RewardedAdCallbackAndListeners.INSTANCE.getOnUserEarnedRewardListener(call, notifyListenersFunction)
                    );
                });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }
}
