package com.getcapacitor.community.admob.helpers;

import android.os.Bundle;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;

public final class RequestHelper {

    private RequestHelper() {}

    /**
     * Use this function to create all requests, here we can centralize request extras
     * @param adOptions
     * @return
     */
    public static AdRequest createRequest(AdOptions adOptions, String adUnitId) {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder(adUnitId);

        // TODO: Allow more key/value extras?
        if (adOptions.npa) {
            Bundle extras = new Bundle();
            extras.putInt("npa", 1);
            adRequestBuilder.setGoogleExtrasBundle(extras);
        }

        return adRequestBuilder.build();
    }

    public static BannerAdRequest createBannerRequest(AdOptions adOptions, String adUnitId, AdSize adSize) {
        BannerAdRequest.Builder adRequestBuilder = new BannerAdRequest.Builder(adUnitId, adSize);

        if (adOptions.npa) {
            Bundle extras = new Bundle();
            extras.putInt("npa", 1);
            adRequestBuilder.setGoogleExtrasBundle(extras);
        }

        return adRequestBuilder.build();
    }
}
