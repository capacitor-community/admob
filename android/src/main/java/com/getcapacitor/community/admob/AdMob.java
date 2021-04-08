package com.getcapacitor.community.admob;

import android.Manifest;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.community.admob.executors.BannerExecutor;
import com.getcapacitor.community.admob.executors.AdRewardExecutor;
import com.getcapacitor.community.admob.executors.AdInterstitialExecutor;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(
        permissions = {
                @Permission(
                    alias = "network",
                    strings = {
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.INTERNET
                    }
                ),
        }
)
public class AdMob extends Plugin {
    public static final JSArray EMPTY_TESTING_DEVICES = new JSArray();

    private BannerExecutor bannerExecutor = new BannerExecutor(this::getContext, this::getActivity, this::notifyListeners, getLogTag());
    private AdRewardExecutor adRewardExecutor = new AdRewardExecutor(
        this::getContext,
        this::getActivity,
        this::notifyListeners,
        getLogTag()
    );
    private AdInterstitialExecutor adInterstitialExecutor = new AdInterstitialExecutor(
            this::getContext,
            this::getActivity,
            this::notifyListeners,
            getLogTag()
    );


    // Initialize AdMob with appId
    @PluginMethod
    public void initialize(final PluginCall call) {
        final boolean initializeForTesting = call.getBoolean("initializeForTesting", false);

        if (initializeForTesting) {
            JSArray testingDevices = call.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES);
            this.setTestingDevicesTo(call, testingDevices);
        } else {
            this.setTestingDevicesTo(call, EMPTY_TESTING_DEVICES);
        }

        try {
            MobileAds.initialize(
                getContext(),
                new OnInitializationCompleteListener() {

                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                }
            );
            bannerExecutor.initialize();
            call.resolve(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
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
        adInterstitialExecutor.showInterstitial(call);
    }

    @PluginMethod
    public void prepareRewardVideoAd(final PluginCall call) {
        adRewardExecutor.prepareRewardVideoAd(call, this::notifyListeners);
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call) {
        adRewardExecutor.showRewardVideoAd(call);
    }

    /**
     * An Array of devices IDs that will be marked as tested devices.
     *
     * @see <a href="https://developers.google.com/admob/android/test-ads#enable_test_devices">Test Devices</a>
     */
    private void setTestingDevicesTo(final PluginCall call, JSArray testingDevices) {
        // TODO: create a function to automatically get the device ID when isTesting is true? https://stackoverflow.com/a/36242494/1255819
        try {
            final RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testingDevices.<String>toList())
                .build();

            MobileAds.setRequestConfiguration(configuration);
        } catch (JSONException error) {
            call.reject(error.toString());
        }
    }
}
