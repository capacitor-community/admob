package com.getcapacitor.community.admob.helpers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.FullScreenAdEventName;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.common.util.BiConsumer;

public final class AdViewIdHelper {

    private AdViewIdHelper() {}

    public static String getFinalAdId(AdOptions adOptions, AdRequest adRequest, String logTag, Context context) {
        if (!adOptions.isTesting) {
            return adOptions.adId;
        }

        if (adRequest.isTestDevice(context)) {
            Log.w(logTag, "This device is registered as Testing Device. The real Ad Id will be used");
            return adOptions.adId;
        }

        return adOptions.getTestingId();
    }

    public static void assignIdToAdView(AdView adView, AdOptions adOptions, AdRequest adRequest, String logTag, Context context) {
        String finalId = getFinalAdId(adOptions, adRequest, logTag, context);
        adView.setAdUnitId(finalId);
        Log.d(logTag, "Ad ID: " + finalId);
    }

    public static FullScreenContentCallback getFullScreenContentCallback(
        BiConsumer<String, JSObject> notifyListenersFunction
    ) {
        return new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(FullScreenAdEventName.adDidPresentFullScreenContent.name(), new JSObject());
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                notifyListenersFunction.accept(
                    FullScreenAdEventName.didFailToPresentFullScreenContentWithError.name(),
                    new JSObject().put("code", 0).put("message", adError.getMessage())
                );
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                notifyListenersFunction.accept(FullScreenAdEventName.adDidDismissFullScreenContent.name(), new JSObject());
            }
        };
    }
}
