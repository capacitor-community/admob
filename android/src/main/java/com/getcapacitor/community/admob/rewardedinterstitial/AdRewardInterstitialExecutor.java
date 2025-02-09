package com.getcapacitor.community.admob.rewardedinterstitial;

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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.common.util.BiConsumer;

public class AdRewardInterstitialExecutor extends Executor {

    public static RewardedInterstitialAd mRewardedInterstitialAd;

    public AdRewardInterstitialExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    @PluginMethod
    public void prepareRewardInterstitialAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions adOptions = AdOptions.getFactory().createRewardInterstitialOptions(call);

        activitySupplier
            .get()
            .runOnUiThread(() -> {
                try {
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                    final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                    RewardedInterstitialAd.load(
                        contextSupplier.get(),
                        id,
                        adRequest,
                        RewardedInterstitialAdCallbackAndListeners.INSTANCE.getRewardedAdLoadCallback(
                            call,
                            notifyListenersFunction,
                            adOptions
                        )
                    );
                } catch (Exception ex) {
                    call.reject(ex.getLocalizedMessage(), ex);
                }
            });
    }

    @PluginMethod
    public void showRewardInterstitialAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        if (mRewardedInterstitialAd == null) {
            String errorMessage = "No Reward Video Ad can be show. It was not prepared or maybe it failed to be prepared.";
            call.reject(errorMessage);
            AdMobPluginError errorObject = new AdMobPluginError(-1, errorMessage);
            notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.FailedToLoad, errorObject);
            return;
        }

        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    mRewardedInterstitialAd.show(
                        activitySupplier.get(),
                        RewardedInterstitialAdCallbackAndListeners.INSTANCE.getOnUserEarnedRewardListener(call, notifyListenersFunction)
                    );
                });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }
}
