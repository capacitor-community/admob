package com.getcapacitor.community.admob.helpers;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardedVideoAd;

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

    // TODO: Is there a way to unify this two overloads?
    /**
     * There are 3 scenarios
     * 1. Real AdId used : We are *NOT* requesting a testing ad.
     * 2. Testing ID used: We request a testing ad with a device not registered as a Testing device
     * 3. Real AdId used: We request a testing ad but this device is a Testing device
     */
    public static void assignIdToAdView(InterstitialAd adView, AdOptions adOptions, AdRequest adRequest, String logTag, Context context) {
        String finalId = getFinalAdId(adOptions, adRequest, logTag, context);
        adView.setAdUnitId(finalId);
        Log.d(logTag, "Ad ID: " + finalId);
    }

    public static void assignIdToAdView(AdView adView, AdOptions adOptions, AdRequest adRequest, String logTag, Context context) {
        String finalId = getFinalAdId(adOptions, adRequest, logTag, context);
        adView.setAdUnitId(finalId);
        Log.d(logTag, "Ad ID: " + finalId);
    }
}
