package com.getcapacitor.community.admob;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.community.admob.appopen.AppOpenAdPlugin;
import com.getcapacitor.community.admob.banner.BannerExecutor;
import com.getcapacitor.community.admob.consent.AdConsentExecutor;
import com.getcapacitor.community.admob.helpers.AuthorizationStatusEnum;
import com.getcapacitor.community.admob.interstitial.AdInterstitialExecutor;
import com.getcapacitor.community.admob.interstitial.InterstitialAdCallbackAndListeners;
import com.getcapacitor.community.admob.rewarded.AdRewardExecutor;
import com.getcapacitor.community.admob.rewardedinterstitial.AdRewardInterstitialExecutor;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import org.json.JSONException;

@CapacitorPlugin(
    permissions = { @Permission(alias = "network", strings = { Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET }) }
)
public class AdMob extends Plugin {

    public static final JSArray EMPTY_TESTING_DEVICES = new JSArray();

    private final BannerExecutor bannerExecutor = new BannerExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag()
    );

    private final AdRewardExecutor adRewardExecutor = new AdRewardExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag()
    );

    private final AdRewardInterstitialExecutor adRewardInterstitialExecutor = new AdRewardInterstitialExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag()
    );

    private final AdInterstitialExecutor adInterstitialExecutor = new AdInterstitialExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag(),
        InterstitialAdCallbackAndListeners.INSTANCE
    );

    private final AdConsentExecutor adConsentExecutor = new AdConsentExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag()
    );

    private final AppOpenAdPlugin appOpenAdPlugin = new AppOpenAdPlugin();

    @PluginMethod
    public void loadAppOpen(final PluginCall call) {
        appOpenAdPlugin.loadAppOpen(getContext(), getActivity(), call, this::notifyListeners);
    }

    @PluginMethod
    public void showAppOpen(final PluginCall call) {
        appOpenAdPlugin.showAppOpen(getActivity(), call, this::notifyListeners);
    }

    @PluginMethod
    public void isAppOpenLoaded(final PluginCall call) {
        appOpenAdPlugin.isAppOpenLoaded(getActivity(), call);
    }

    // ---------------------------------------------------------
    // MAIN METHODS
    // ---------------------------------------------------------

    @PluginMethod
    public void initialize(final PluginCall call) {
        final String appId = getAdMobAppId(call);
        if (appId == null || appId.trim().isEmpty()) {
            call.reject(
                "AdMob appId not found. " +
                    "Please configure AndroidManifest meta-data " +
                    "\"com.google.android.gms.ads.APPLICATION_ID\" (see docs/installation.md)."
            );
            return;
        }
        final RequestConfiguration requestConfiguration = this.createRequestConfiguration(call);

        new Thread(() -> {
            try {
                MobileAds.initialize(
                    getContext(),
                    new InitializationConfig.Builder(appId).setRequestConfiguration(requestConfiguration).build(),
                    (initializationStatus) -> {}
                );
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(bannerExecutor::initialize);
                } else {
                    new Handler(Looper.getMainLooper()).post(bannerExecutor::initialize);
                }
                call.resolve();
            } catch (Exception ex) {
                call.reject(ex.getLocalizedMessage(), ex);
            }
        })
            .start();
    }

    @PluginMethod
    public void requestTrackingAuthorization(final PluginCall call) {
        call.resolve();
    }

    @PluginMethod
    public void trackingAuthorizationStatus(final PluginCall call) {
        JSObject response = new JSObject();
        response.put("status", AuthorizationStatusEnum.AUTHORIZED.getStatus());
        call.resolve(response);
    }

    // ---------------------------------------------------------
    // USER CONSENT
    // ---------------------------------------------------------

    @PluginMethod
    public void requestConsentInfo(final PluginCall call) {
        adConsentExecutor.requestConsentInfo(call, this::notifyListeners);
    }

    @PluginMethod
    public void showPrivacyOptionsForm(final PluginCall call) {
        adConsentExecutor.showPrivacyOptionsForm(call, this::notifyListeners);
    }

    @PluginMethod
    public void showConsentForm(final PluginCall call) {
        adConsentExecutor.showConsentForm(call, this::notifyListeners);
    }

    @PluginMethod
    public void resetConsentInfo(final PluginCall call) {
        adConsentExecutor.resetConsentInfo(call, this::notifyListeners);
    }

    // ---------------------------------------------------------
    // APP SETTINGS
    // ---------------------------------------------------------

    @PluginMethod
    public void setApplicationMuted(final PluginCall call) {
        Boolean muted = call.getBoolean("muted");
        if (muted == null) {
            call.reject("muted property cannot be null");
            return;
        }
        // Next-Gen SDK removed global mute setter. Keep API call non-breaking.
        call.resolve();
    }

    @PluginMethod
    public void setApplicationVolume(final PluginCall call) {
        Float volume = call.getFloat("volume");
        if (volume == null) {
            call.reject("volume property cannot be null");
            return;
        }
        // Next-Gen SDK removed global volume setter. Keep API call non-breaking.
        call.resolve();
    }

    // ---------------------------------------------------------
    // BANNER ADS
    // ---------------------------------------------------------

    @PluginMethod
    public void showBanner(final PluginCall call) {
        bannerExecutor.showBanner(call);
    }

    @PluginMethod
    public void hideBanner(final PluginCall call) {
        bannerExecutor.hideBanner(call);
    }

    @PluginMethod
    public void resumeBanner(final PluginCall call) {
        bannerExecutor.resumeBanner(call);
    }

    @PluginMethod
    public void removeBanner(final PluginCall call) {
        bannerExecutor.removeBanner(call);
    }

    // ---------------------------------------------------------
    // INTERSTITIAL ADS
    // ---------------------------------------------------------

    @PluginMethod
    public void prepareInterstitial(final PluginCall call) {
        adInterstitialExecutor.prepareInterstitial(call, this::notifyListeners);
    }

    @PluginMethod
    public void showInterstitial(final PluginCall call) {
        adInterstitialExecutor.showInterstitial(call, this::notifyListeners);
    }

    // ---------------------------------------------------------
    // REWARDED ADS
    // ---------------------------------------------------------

    @PluginMethod
    public void prepareRewardVideoAd(final PluginCall call) {
        adRewardExecutor.prepareRewardVideoAd(call, this::notifyListeners);
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call) {
        adRewardExecutor.showRewardVideoAd(call, this::notifyListeners);
    }

    @PluginMethod
    public void prepareRewardInterstitialAd(final PluginCall call) {
        adRewardInterstitialExecutor.prepareRewardInterstitialAd(call, this::notifyListeners);
    }

    @PluginMethod
    public void showRewardInterstitialAd(final PluginCall call) {
        adRewardInterstitialExecutor.showRewardInterstitialAd(call, this::notifyListeners);
    }

    // ---------------------------------------------------------
    // REQUEST CONFIGURATION
    // ---------------------------------------------------------

    private RequestConfiguration createRequestConfiguration(final PluginCall call) {
        // Testing Devices
        final boolean initializeForTesting = call.getBoolean("initializeForTesting", false);
        final JSArray testingDevices = initializeForTesting
            ? call.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)
            : EMPTY_TESTING_DEVICES;

        // tagForChildDirectedTreatment
        final Boolean tagForChildDirectedTreatment = call.getBoolean("tagForChildDirectedTreatment");
        RequestConfiguration.TagForChildDirectedTreatment tagForChildDirectedTreatmentValue;

        if (tagForChildDirectedTreatment == null) {
            tagForChildDirectedTreatmentValue =
                RequestConfiguration.TagForChildDirectedTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
        } else if (tagForChildDirectedTreatment) {
            tagForChildDirectedTreatmentValue = RequestConfiguration.TagForChildDirectedTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;
        } else {
            tagForChildDirectedTreatmentValue = RequestConfiguration.TagForChildDirectedTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
        }

        // tagForUnderAgeOfConsent
        final Boolean tagForUnderAgeOfConsent = call.getBoolean("tagForUnderAgeOfConsent");
        RequestConfiguration.TagForUnderAgeOfConsent tagForUnderAgeOfConsentValue;

        if (tagForUnderAgeOfConsent == null) {
            tagForUnderAgeOfConsentValue = RequestConfiguration.TagForUnderAgeOfConsent.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED;
        } else if (tagForUnderAgeOfConsent) {
            tagForUnderAgeOfConsentValue = RequestConfiguration.TagForUnderAgeOfConsent.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE;
        } else {
            tagForUnderAgeOfConsentValue = RequestConfiguration.TagForUnderAgeOfConsent.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE;
        }

        // maxAdContentRating
        final String maxAdContentRating = call.getString("maxAdContentRating");
        RequestConfiguration.MaxAdContentRating maxAdContentRatingValue =
            RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_UNSPECIFIED;

        if (maxAdContentRating != null) {
            switch (maxAdContentRating) {
                case "General":
                    maxAdContentRatingValue = RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_G;
                    break;
                case "ParentalGuidance":
                    maxAdContentRatingValue = RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_PG;
                    break;
                case "Teen":
                    maxAdContentRatingValue = RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_T;
                    break;
                case "MatureAudience":
                    maxAdContentRatingValue = RequestConfiguration.MaxAdContentRating.MAX_AD_CONTENT_RATING_MA;
                    break;
            }
        }

        try {
            RequestConfiguration requestConfiguration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testingDevices.<String>toList())
                .setTagForChildDirectedTreatment(tagForChildDirectedTreatmentValue)
                .setTagForUnderAgeOfConsent(tagForUnderAgeOfConsentValue)
                .setMaxAdContentRating(maxAdContentRatingValue)
                .build();
            return requestConfiguration;
        } catch (JSONException error) {
            call.reject(error.toString());
            return new RequestConfiguration.Builder().build();
        }
    }

    private String getAdMobAppId(final PluginCall call) {
        try {
            if (getContext() == null) return null;

            ApplicationInfo appInfo = getContext()
                .getPackageManager()
                .getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData == null) return null;

            final String key = "com.google.android.gms.ads.APPLICATION_ID";
            final Object appIdValue = appInfo.metaData.get(key);
            if (appIdValue instanceof String) {
                return (String) appIdValue;
            }

            final int appIdResId = appInfo.metaData.getInt(key, 0);
            if (appIdResId == 0) return null;

            Resources resources = getContext().getResources();
            return resources.getString(appIdResId);
        } catch (Exception e) {
            return null;
        }
    }
}
