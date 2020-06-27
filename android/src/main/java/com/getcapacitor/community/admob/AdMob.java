package com.getcapacitor.community.admob;

import android.Manifest;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


@NativePlugin(
        permissions = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        }
)
public class AdMob extends Plugin {

    private PluginCall call;
    private ViewGroup mViewGroup;
    private RelativeLayout mAdViewLayout;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    // Initialize AdMob with appId
    @PluginMethod()
    public void initialize(PluginCall call) {
        try {
            MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            mViewGroup = (ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0);
            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    // Show a banner Ad
    @PluginMethod()
    public void showBanner(PluginCall call) {
        String adId       = call.getString("adId", "ca-app-pub-3940256099942544/6300978111");
        String adSize     = call.getString("adSize", "SMART_BANNER");
        String adPosition = call.getString("position", "BOTTOM_CENTER");
        int adMargin      = call.getInt("margin", 0);
        boolean isTesting  = call.getBoolean("isTesting", false);

        if (isTesting) {
            Log.d(getLogTag(), "TESTING");
            adId = "ca-app-pub-3940256099942544/6300978111";
        }

        if (mAdView != null) {
            return;
        }

        try {
            mAdView = new AdView(getContext());
            mAdView.setAdUnitId(adId);
            Log.d(getLogTag(), "Ad ID: " + adId);

            switch (adSize) {
                case "BANNER":
                    mAdView.setAdSize(AdSize.BANNER);
                    break;
                case "FLUID":
                    mAdView.setAdSize(AdSize.FLUID);
                    break;
                case "FULL_BANNER":
                    mAdView.setAdSize(AdSize.FULL_BANNER);
                    break;
                case "LARGE_BANNER":
                    mAdView.setAdSize(AdSize.LARGE_BANNER);
                    break;
                case "LEADERBOARD":
                    mAdView.setAdSize(AdSize.LEADERBOARD);
                    break;
                case "MEDIUM_RECTANGLE":
                    mAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    break;
                default:
                    mAdView.setAdSize(AdSize.SMART_BANNER);
                    break;
            }

            // Setup AdView Layout
            mAdViewLayout = new RelativeLayout(getContext());
            mAdViewLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            mAdViewLayout.setVerticalGravity(Gravity.BOTTOM);

            final CoordinatorLayout.LayoutParams mAdViewLayoutParams = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            switch (adPosition) {
                case "TOP_CENTER":
                    mAdViewLayoutParams.gravity = Gravity.TOP;
                    break;
                case "CENTER":
                    mAdViewLayoutParams.gravity = Gravity.CENTER;
                    break;
                default:
                    mAdViewLayoutParams.gravity = Gravity.BOTTOM;
                    break;
            }

            mAdViewLayout.setLayoutParams(mAdViewLayoutParams);

            float density = getContext().getResources().getDisplayMetrics().density;
            int densityMargin = (int) (adMargin * density);
            mAdViewLayoutParams.setMargins(0, densityMargin, 0, densityMargin);

            // Run AdMob In Main UI Thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

                    // Add the AdView to the view hierarchy.
                    mAdViewLayout.addView(mAdView);

                    // Start loading the ad.
                    mAdView.loadAd(adRequestBuilder.build());

                    mAdView.setAdListener(new AdListener(){
                        @Override
                        public void onAdLoaded() {
                            notifyListeners("onAdLoaded", new JSObject().put("value", true));

                            JSObject ret = new JSObject();
                            ret.put("width", mAdView.getAdSize().getWidth());
                            ret.put("height", mAdView.getAdSize().getHeight());
                            notifyListeners("onAdSize", ret);

                            super.onAdLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            notifyListeners("onAdFailedToLoad", new JSObject().put("errorCode", i));

                            JSObject ret = new JSObject();
                            ret.put("width", 0);
                            ret.put("height", 0);
                            notifyListeners("onAdSize", ret);

                            super.onAdFailedToLoad(i);
                        }

                        @Override
                        public void onAdOpened() {
                            notifyListeners("onAdOpened", new JSObject().put("value", true));
                            super.onAdOpened();
                        }

                        @Override
                        public void onAdClosed() {
                            notifyListeners("onAdClosed", new JSObject().put("value", true));
                            super.onAdClosed();
                        }
                    });

                    // Add AdViewLayout top of the WebView
                    mViewGroup.addView(mAdViewLayout);
                }
            });

            call.success(new JSObject().put("value", true));

        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Hide the banner, remove it from screen, but can show it later
    @PluginMethod()
    public void hideBanner(PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdViewLayout != null) {
                        mAdViewLayout.setVisibility(View.GONE);
                        mAdView.pause();
                    }
                }
            });

            JSObject ret = new JSObject();
            ret.put("width", 0);
            ret.put("height", 0);
            notifyListeners("onAdSize", ret);

            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Resume the banner, show it after hide
    @PluginMethod()
    public void resumeBanner(PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdViewLayout != null && mAdView != null) {
                        mAdViewLayout.setVisibility(View.VISIBLE);
                        mAdView.resume();
                        Log.d(getLogTag(), "Banner AD Resumed");
                    }
                }
            });

            JSObject ret = new JSObject();
            ret.put("width", mAdView.getAdSize().getWidth());
            ret.put("height", mAdView.getAdSize().getHeight());
            notifyListeners("onAdSize", ret);

            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Destroy the banner, remove it from screen.
    @PluginMethod()
    public void removeBanner(PluginCall call) {
        try {
            if (mAdView != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdView != null) {
                            mViewGroup.removeView(mAdViewLayout);
                            mAdViewLayout.removeView(mAdView);
                            mAdView.destroy();
                            mAdView = null;
                            Log.d(getLogTag(), "Banner AD Removed");
                        }
                    }
                });
            }

            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }




    // Prepare interstitial Ad
    @PluginMethod()
    public void prepareInterstitial(final PluginCall call) {
        this.call = call;
        String adId = call.getString("adId", "ca-app-pub-3940256099942544/1033173712");

        boolean isTesting  = call.getBoolean("isTesting", false);

        if (isTesting) {
            Log.d(getLogTag(), "TESTING");
            adId = "ca-app-pub-3940256099942544/1033173712";
        }

        try {
            mInterstitialAd = new InterstitialAd(getContext());
            mInterstitialAd.setAdUnitId(adId);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());

                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            notifyListeners("onAdLoaded", new JSObject().put("value", true));
                            call.success(new JSObject().put("value", true));
                            super.onAdLoaded();

                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // Code to be executed when an ad request fails.
                            notifyListeners("onAdFailedToLoad", new JSObject().put("errorCode", errorCode));
                            super.onAdFailedToLoad(errorCode);
                        }

                        @Override
                        public void onAdOpened() {
                            // Code to be executed when the ad is displayed.
                            notifyListeners("onAdOpened", new JSObject().put("value", true));
                            super.onAdOpened();
                        }

                        @Override
                        public void onAdLeftApplication() {
                            // Code to be executed when the user has left the app.
                            notifyListeners("onAdLeftApplication", new JSObject().put("value", true));
                            super.onAdLeftApplication();
                        }

                        @Override
                        public void onAdClosed() {
                            // Code to be executed when when the interstitial ad is closed.
                            notifyListeners("onAdClosed", new JSObject().put("value", true));
                            super.onAdClosed();
                        }
                    });

                }
            });

        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Show interstitial Ad
    @PluginMethod()
    public void showInterstitial(final PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mInterstitialAd.show();
                            }
                        });
                        call.success(new JSObject().put("value", true));
                    } else {
                        call.error("The interstitial wasn't loaded yet.");
                    }
                }
            });
        }catch (Exception ex){
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    // Prepare a RewardVideoAd
    @PluginMethod()
    public void prepareRewardVideoAd(final PluginCall call) {
        this.call = call;
        /* dedicated test ad unit ID for Android rewarded video:
            ca-app-pub-3940256099942544/5224354917
        */

        String _adId = call.getString("adId", "ca-app-pub-3940256099942544/5224354917");

        boolean isTesting  = call.getBoolean("isTesting", false);
        if (isTesting) {
            Log.d(getLogTag(), "TESTING");
            _adId = "ca-app-pub-3940256099942544/5224354917";
        }

        final String adId = _adId;

        try {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.loadAd(adId, new AdRequest.Builder().build());

                    mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                        @Override
                        public void onRewardedVideoAdLoaded() {
                            call.success(new JSObject().put("value", true));
                            notifyListeners("onRewardedVideoAdLoaded", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoAdOpened() {
                            notifyListeners("onRewardedVideoAdOpened", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoStarted() {
                            notifyListeners("onRewardedVideoStarted", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoAdClosed() {
                            notifyListeners("onRewardedVideoAdClosed", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewarded(RewardItem rewardItem) {
                            notifyListeners("onRewarded", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoAdLeftApplication() {
                            notifyListeners("onRewardedVideoAdLeftApplication", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoAdFailedToLoad(int i) {
                            notifyListeners("onRewardedVideoAdFailedToLoad", new JSObject().put("value", true));
                        }

                        @Override
                        public void onRewardedVideoCompleted() {
                            notifyListeners("onRewardedVideoCompleted", new JSObject().put("value", true));
                        }
                    });
                }
            });

        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }

    }

    // Show a RewardVideoAd
    @PluginMethod()
    public void showRewardVideoAd(final PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRewardedVideoAd != null && mRewardedVideoAd.isLoaded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRewardedVideoAd.show();
                            }
                        });
                        call.success(new JSObject().put("value", true));
                    }else {
                        call.error("The RewardedVideoAd wasn't loaded yet.");
                    }
                }
            });

        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Pause a RewardVideoAd
    @PluginMethod()
    public void pauseRewardedVideo(PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.pause(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    // Resume a RewardVideoAd
    @PluginMethod()
    public void resumeRewardedVideo(PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.resume(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }

    // Destroy a RewardVideoAd
    @PluginMethod()
    public void stopRewardedVideo(PluginCall call) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.destroy(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        }catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }
}
