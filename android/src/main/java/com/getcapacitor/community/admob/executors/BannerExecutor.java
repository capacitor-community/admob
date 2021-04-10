package com.getcapacitor.community.admob.executors;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.common.util.BiConsumer;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import com.google.android.gms.ads.MobileAds;

public class BannerExecutor extends Executor {

    private RelativeLayout mAdViewLayout;
    private AdView mAdView;
    private ViewGroup mViewGroup;

    public BannerExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "BannerExecutor");
    }

    public void initialize() {
        mViewGroup = (ViewGroup) ((ViewGroup) activitySupplier.get().findViewById(android.R.id.content)).getChildAt(0);
    }

    public void showBanner(final PluginCall call) {
        final AdOptions adOptions = AdOptions.getFactory().createBannerOptions(call);
        float widthPixels = (int)contextSupplier.get().getResources().getDisplayMetrics().widthPixels;
        float density = contextSupplier.get().getResources().getDisplayMetrics().density;

        if (mAdView != null) {
            updateExistingAdView(adOptions);
            return;
        }

        // Why a try catch block?
        try {
            mAdView = new AdView(contextSupplier.get());

            if (!adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                mAdView.setAdSize(adOptions.adSize.size);
            } else {
                // ADAPTIVE BANNER
                mAdView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(contextSupplier.get(), (int) (widthPixels / density)));
            }

            // Setup AdView Layout
            mAdViewLayout = new RelativeLayout(contextSupplier.get());
            mAdViewLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            mAdViewLayout.setVerticalGravity(Gravity.BOTTOM);

            final CoordinatorLayout.LayoutParams mAdViewLayoutParams = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            // TODO: Make an enum like the AdSizeEnum?
            switch (adOptions.position) {
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

            int densityMargin = (int) (adOptions.margin * density);

            // Center Banner Ads
            int adWidth = (int) (adOptions.adSize.size.getWidth() * density);
            int sideMargin = ((int) widthPixels - adWidth) / 2;

            if (adWidth <= 0 || adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                mAdViewLayoutParams.setMargins(0, densityMargin, 0, densityMargin);
            } else {
                mAdViewLayoutParams.setMargins(sideMargin, densityMargin, sideMargin, densityMargin);
            }

            createNewAdView(adOptions);

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void hideBanner(final PluginCall call) {
        if (mAdView == null) {
            call.reject("You tried to hide a banner that was never shown");
            return;
        }

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        if (mAdViewLayout != null) {
                            mAdViewLayout.setVisibility(View.GONE);
                            mAdView.pause();

                            JSObject ret = new JSObject();
                            ret.put("width", 0);
                            ret.put("height", 0);
                            notifyListeners("bannerViewChangeSize", ret);

                            call.resolve();
                        }
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void resumeBanner(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        if (mAdViewLayout != null && mAdView != null) {
                            mAdViewLayout.setVisibility(View.VISIBLE);
                            mAdView.resume();

                            JSObject ret = new JSObject();
                            ret.put("width", mAdView.getAdSize().getWidth());
                            ret.put("height", mAdView.getAdSize().getHeight());
                            notifyListeners("bannerViewChangeSize", ret);

                            Log.d(logTag, "Banner AD Resumed");
                        }
                    }
                );

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void removeBanner(final PluginCall call) {
        try {
            if (mAdView != null) {
                activitySupplier
                    .get()
                    .runOnUiThread(
                        () -> {
                            if (mAdView != null) {
                                mViewGroup.removeView(mAdViewLayout);
                                mAdViewLayout.removeView(mAdView);
                                mAdView.destroy();
                                mAdView = null;
                                Log.d(logTag, "Banner AD Removed");
                            }
                        }
                    );
            }

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    private void updateExistingAdView(AdOptions adOptions) {
        activitySupplier
            .get()
            .runOnUiThread(
                () -> {
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                    mAdView.loadAd(adRequest);
                }
            );
    }


    /**
     * Follow iOS method Name:
     * https://developers.google.com/admob/ios/banner?hl=ja
     */
    private void createNewAdView(AdOptions adOptions) {
        // Run AdMob In Main UI Thread
        activitySupplier
            .get()
            .runOnUiThread(
                () -> {
                    final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                    // Assign the correct id needed
                    AdViewIdHelper.assignIdToAdView(mAdView, adOptions, adRequest, logTag, contextSupplier.get());
                    // Add the AdView to the view hierarchy.
                    mAdViewLayout.addView(mAdView);
                    // Start loading the ad.
                    mAdView.loadAd(adRequest);
                    mAdView.setAdListener(
                        new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                notifyListenersFunction.accept("bannerViewDidReceiveAd", new JSObject());

                                JSObject ret = new JSObject();
                                ret.put("width", mAdView.getAdSize().getWidth());
                                ret.put("height", mAdView.getAdSize().getHeight());
                                notifyListeners("bannerViewChangeSize", ret);

                                super.onAdLoaded();
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError AdError) {
                                notifyListeners("bannerView:didFailToReceiveAdWithError", new JSObject().put("errorCode", AdError.getCode()));

                                JSObject ret = new JSObject();
                                ret.put("width", 0);
                                ret.put("height", 0);
                                notifyListeners("bannerViewDidReceiveAd", ret);

                                super.onAdFailedToLoad(AdError);
                            }

                            @Override
                            public void onAdOpened() {
                                notifyListeners("bannerViewWillPresentScreen", new JSObject());
                                super.onAdOpened();
                            }

                            @Override
                            public void onAdClosed() {
                                notifyListeners("bannerViewWillDismissScreen", new JSObject());
                                super.onAdClosed();
                            }
                        }
                    );

                    // Add AdViewLayout top of the WebView
                    mViewGroup.addView(mAdViewLayout);
                }
            );
    }
}
