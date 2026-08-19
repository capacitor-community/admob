package com.getcapacitor.community.admob.interstitial;

import android.app.Activity;
import android.content.Context;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class AdInterstitialExecutor extends Executor {

    public static final Map<String, InterstitialAd> preparedAds = new LinkedHashMap<>();
    public static String lastPreparedAdId;

    InterstitialAdCallbackAndListeners adCallbackAndListeners;

    public AdInterstitialExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag,
        InterstitialAdCallbackAndListeners adCallbackAndListeners
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
        this.adCallbackAndListeners = adCallbackAndListeners;
    }

    public void prepareInterstitial(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions.AdOptionsFactory factory = AdOptions.getFactory();
        final AdOptions adOptions = factory.createInterstitialOptions(call);

        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    final String id = AdViewIdHelper.getFinalAdId(adOptions, logTag, contextSupplier.get());
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions, id);
                    InterstitialAd.load(adRequest, adCallbackAndListeners.getInterstitialAdLoadCallback(call, notifyListenersFunction, id));
                });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void showInterstitial(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        String requestedAdId = call.getString("adId");
        String adId = requestedAdId != null ? requestedAdId : lastPreparedAdId;
        InterstitialAd ad = adId != null ? preparedAds.get(adId) : null;

        if (ad == null) {
            String errorMessage = "No Interstitial can be shown. It was not prepared or maybe it failed to be prepared.";
            call.reject(errorMessage);
            AdMobPluginError errorObject = new AdMobPluginError(-1, errorMessage);
            notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.FailedToLoad, errorObject);
            return;
        }

        final InterstitialAd adToShow = ad;
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                try {
                    adToShow.setAdEventCallback(
                        adCallbackAndListeners.getInterstitialAdEventCallback(notifyListenersFunction, () -> {
                            preparedAds.remove(adId);
                            if (adId != null && adId.equals(lastPreparedAdId)) {
                                lastPreparedAdId = null;
                                for (String remainingAdId : preparedAds.keySet()) lastPreparedAdId = remainingAdId;
                            }
                        })
                    );
                    adToShow.show(activitySupplier.get());
                    call.resolve();
                } catch (Exception ex) {
                    call.reject(ex.getLocalizedMessage(), ex);
                }
            });
    }
}
