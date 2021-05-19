package com.getcapacitor.community.admob.helpers;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
}
