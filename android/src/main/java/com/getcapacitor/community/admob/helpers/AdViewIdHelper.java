package com.getcapacitor.community.admob.helpers;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;

public final class AdViewIdHelper {

    private AdViewIdHelper() {}

    public static String getFinalAdId(AdOptions adOptions, String logTag, Context context) {
        if (!adOptions.isTesting) {
            return adOptions.adId;
        }

        if (context != null && MobileAds.getRequestConfiguration().isTestDevice(context)) {
            Log.w(logTag, "This device is registered as Testing Device. The real Ad Id will be used");
            return adOptions.adId;
        }

        return adOptions.getTestingId();
    }

    public static String assignIdToAdView(AdView adView, AdOptions adOptions, String logTag, Context context) {
        String finalId = getFinalAdId(adOptions, logTag, context);
        Log.d(logTag, "Ad ID: " + finalId);
        return finalId;
    }
}
