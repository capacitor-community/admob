package com.getcapacitor.community.admob.interstitial;

import android.app.Activity;
import android.content.Context;

import androidx.core.util.Supplier;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.common.util.BiConsumer;

public class AdInterstitialExecutor extends Executor {

    public static InterstitialAd interstitialAd;

    public AdInterstitialExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    public void prepareInterstitial(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions.AdOptionsFactory factory = AdOptions.getFactory();
        final AdOptions adOptions = factory.createInterstitialOptions(call);

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {

                        final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                        final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                        InterstitialAd.load(
                            activitySupplier.get(),
                            id,
                            adRequest,
                                InterstitialAdCallbackAndListeners.INSTANCE.getInterstitialAdLoadCallback(call, notifyListenersFunction)
                        );
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }


    public void showInterstitial(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        interstitialAd.show(activitySupplier.get());
                        call.resolve();
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }




}
