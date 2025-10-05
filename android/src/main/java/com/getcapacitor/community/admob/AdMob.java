package com.getcapacitor.community.admob;

import android.Manifest;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.community.admob.banner.BannerExecutor;
import com.getcapacitor.community.admob.consent.AdConsentExecutor;
import com.getcapacitor.community.admob.helpers.AuthorizationStatusEnum;
import com.getcapacitor.community.admob.interstitial.AdInterstitialExecutor;
import com.getcapacitor.community.admob.interstitial.InterstitialAdCallbackAndListeners;
import com.getcapacitor.community.admob.rewarded.AdRewardExecutor;
import com.getcapacitor.community.admob.rewardedinterstitial.AdRewardInterstitialExecutor;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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

    // Initialize AdMob with appId
    @PluginMethod
    public void initialize(final PluginCall call) {
        this.setRequestConfiguration(call);

        try {
            MobileAds.initialize(
                getContext(),
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                }
            );
            bannerExecutor.initialize();
            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
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

    // User Consent
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

    @PluginMethod
    public void setApplicationMuted(final PluginCall call) {
        Boolean muted = call.getBoolean("muted");
        if (muted == null) {
            call.reject("muted property cannot be null");
            return;
        }
        MobileAds.setAppMuted(muted);
        call.resolve();
    }

    @PluginMethod
    public void setApplicationVolume(final PluginCall call) {
        Float volume = call.getFloat("volume");
        if (volume == null) {
            call.reject("volume property cannot be null");
            return;
        }
        MobileAds.setAppVolume(volume);
        call.resolve();
    }

    // Show a banner Ad
    @PluginMethod
    public void showBanner(final PluginCall call) {
        bannerExecutor.showBanner(call);
    }

    // Hide the banner, remove it from screen, but can show it later
    @PluginMethod
    public void hideBanner(final PluginCall call) {
        bannerExecutor.hideBanner(call);
    }

    // Resume the banner, show it after hide
    @PluginMethod
    public void resumeBanner(final PluginCall call) {
        bannerExecutor.resumeBanner(call);
    }

    // Destroy the banner, remove it from screen.
    @PluginMethod
    public void removeBanner(final PluginCall call) {
        bannerExecutor.removeBanner(call);
    }

    @PluginMethod
    public void prepareInterstitial(final PluginCall call) {
        adInterstitialExecutor.prepareInterstitial(call, this::notifyListeners);
    }

    // Show interstitial Ad
    @PluginMethod
    public void showInterstitial(final PluginCall call) {
        adInterstitialExecutor.showInterstitial(call, this::notifyListeners);
    }

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

    /**
     * @see <a href="https://developers.google.com/admob/android/test-ads#enable_test_devices">Test Devices</a>
     * @see <a href="https://developers.google.com/admob/android/targeting">Target Settings</a>
     */
    private void setRequestConfiguration(final PluginCall call) {
        // Testing Devices
        final boolean initializeForTesting = call.getBoolean("initializeForTesting", false);
        final JSArray testingDevices = initializeForTesting
            ? call.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)
            : EMPTY_TESTING_DEVICES;

        // tagForChildDirectedTreatment
        final Boolean tagForChildDirectedTreatment = call.getBoolean("tagForChildDirectedTreatment");
        int TAG_FOR_CHILD_DIRECTED_TREATMENT;

        if (tagForChildDirectedTreatment == null) {
            TAG_FOR_CHILD_DIRECTED_TREATMENT = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
        } else if (tagForChildDirectedTreatment) {
            TAG_FOR_CHILD_DIRECTED_TREATMENT = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;
        } else {
            TAG_FOR_CHILD_DIRECTED_TREATMENT = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
        }

        // tagForUnderAgeOfConsent
        final Boolean tagForUnderAgeOfConsent = call.getBoolean("tagForUnderAgeOfConsent");
        int TAG_FOR_UNDER_AGE_OF_CONSENT;

        if (tagForUnderAgeOfConsent == null) {
            TAG_FOR_UNDER_AGE_OF_CONSENT = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED;
        } else if (tagForUnderAgeOfConsent) {
            TAG_FOR_UNDER_AGE_OF_CONSENT = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE;
        } else {
            TAG_FOR_UNDER_AGE_OF_CONSENT = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE;
        }

        // maxAdContentRating
        final String maxAdContentRating = call.getString("maxAdContentRating");
        String MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_UNSPECIFIED;

        if (maxAdContentRating != null) {
            switch (maxAdContentRating) {
                case "General":
                    MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_G;
                    break;
                case "ParentalGuidance":
                    MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_PG;
                    break;
                case "Teen":
                    MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_T;
                    break;
                case "MatureAudience":
                    MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_MA;
                    break;
            }
        }

        try {
            RequestConfiguration requestConfiguration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testingDevices.<String>toList())
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT)
                .setTagForUnderAgeOfConsent(TAG_FOR_UNDER_AGE_OF_CONSENT)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING)
                .build();
            MobileAds.setRequestConfiguration(requestConfiguration);
        } catch (JSONException error) {
            call.reject(error.toString());
        }
    }
}
