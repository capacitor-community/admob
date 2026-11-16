package com.getcapacitor.community.admob.interstitial;

import android.app.Activity;
import android.content.Context;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdExpiration;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.common.util.BiConsumer;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdInterstitialExecutor extends Executor {

    public static final Map<String, InterstitialAd> preparedAds = new LinkedHashMap<>();
    public static String lastPreparedAdId;
    public static final Map<String, AdExpiration> preparedAdExpirations = new LinkedHashMap<>();

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
            activitySupplier.get().runOnUiThread(() -> {
                final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                InterstitialAd.load(
                    activitySupplier.get(),
                    id,
                    adRequest,
                    adCallbackAndListeners.getInterstitialAdLoadCallback(call, notifyListenersFunction)
                );
            });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void showInterstitial(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        String requestedAdId = call.getString("adId");
        String adId = requestedAdId != null ? requestedAdId : lastPreparedAdId;
        InterstitialAd ad = adId != null ? preparedAds.get(adId) : null;

        AdExpiration expiration = preparedAdExpirations.get(adId);
        if (ad == null) {
            String errorMessage = "No Interstitial can be shown. It was not prepared or maybe it failed to be prepared.";
            call.reject(errorMessage);
            AdMobPluginError errorObject = new AdMobPluginError(-1, errorMessage);
            notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.FailedToLoad, errorObject);
            return;
        }

        final InterstitialAd adToShow = ad;
        activitySupplier.get().runOnUiThread(() -> {
            try {
                if (expiration != null && expiration.isExpired()) {
                    removePreparedAd(adId, adToShow);
                    String message = "Interstitial ad expired. Prepare a new ad before showing it.";
                    call.reject(message, "AD_EXPIRED");
                    notifyListenersFunction.accept(
                        InterstitialAdPluginPluginEvent.INSTANCE.getFailedToShow(),
                        new AdMobPluginError(-1, message)
                    );
                    return;
                }
                adToShow.setFullScreenContentCallback(
                    new FullscreenPluginCallback(InterstitialAdPluginPluginEvent.INSTANCE, notifyListenersFunction, () -> {
                        removePreparedAd(adId, adToShow);
                    })
                );
                adToShow.show(activitySupplier.get());
                call.resolve();
            } catch (Exception ex) {
                call.reject(ex.getLocalizedMessage(), ex);
            }
        });
    }

    private static void removePreparedAd(String adId, InterstitialAd ad) {
        if (!preparedAds.remove(adId, ad)) return;
        preparedAdExpirations.remove(adId);
        if (adId.equals(lastPreparedAdId)) {
            lastPreparedAdId = null;
            for (String remainingAdId : preparedAds.keySet()) lastPreparedAdId = remainingAdId;
        }
    }
}
