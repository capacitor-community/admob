package com.getcapacitor.community.admob.helpers;

import android.os.Bundle;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

public final class RequestHelper {

    private RequestHelper() {}

    /**
     * Use this function to create all requests, here we can centralize request extras
     * @param adOptions
     * @return
     */
    public static AdRequest createRequest(AdOptions adOptions) {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        // TODO: Allow more key/value extras?
        if (adOptions.npa) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }

        return adRequestBuilder.build();
    }

    /**
     * Gets a string error reason from an error code.
     */
    public static String getRequestErrorReason(int errorCode) {
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                return "Internal error";
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                return "Invalid request";
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                return "Network Error";
            case AdRequest.ERROR_CODE_NO_FILL:
                return "No fill";
            case AdRequest.ERROR_CODE_APP_ID_MISSING:
                return "App Id Missing";
            default:
                return "Unknown error";
        }
    }
}
