package com.getcapacitor.community.admob.banner;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.common.util.BiConsumer;

public class BannerExecutor extends Executor {

    private final JSObject emptyObject = new JSObject();
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
        float density = contextSupplier.get().getResources().getDisplayMetrics().density;

        int defaultWidthPixels = contextSupplier.get().getResources().getDisplayMetrics().widthPixels;

        DisplayMetrics metrics = new DisplayMetrics();
        activitySupplier.get().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realWidthPixels = metrics.widthPixels;

        boolean fullscreen = false;
        if ((activitySupplier.get().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
            fullscreen = true;
        }

        if (mAdView != null) {
            updateExistingAdView(adOptions);
            return;
        }

        // Why a try catch block?
        try {
            mAdView = new AdView(contextSupplier.get());

            if (!adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                mAdView.setAdSize(adOptions.adSize.getSize());
            } else {
                // ADAPTIVE BANNER
                mAdView.setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(contextSupplier.get(), (int) (defaultWidthPixels / density))
                );
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
            int adWidth = (int) (adOptions.adSize.getSize().getWidth() * density);

            if (adWidth <= 0 || adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                int margin = 0;
                if (fullscreen) {
                    margin = (realWidthPixels - defaultWidthPixels) / 2;
                }
                mAdViewLayoutParams.setMargins(margin, densityMargin, margin, densityMargin);
            } else {
                int sideMargin = ((int) defaultWidthPixels - adWidth) / 2;
                if (fullscreen) {
                    sideMargin = (realWidthPixels - adWidth) / 2;
                }
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
                .runOnUiThread(() -> {
                    if (mAdViewLayout != null) {
                        mAdViewLayout.setVisibility(View.GONE);
                        mAdView.pause();

                        final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);

                        notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                        call.resolve();
                    }
                });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void resumeBanner(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    if (mAdViewLayout != null && mAdView != null) {
                        mAdViewLayout.setVisibility(View.VISIBLE);
                        mAdView.resume();

                        final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(mAdView);
                        notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                        Log.d(logTag, "Banner AD Resumed");
                    }
                });

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
                    .runOnUiThread(() -> {
                        if (mAdView != null) {
                            mViewGroup.removeView(mAdViewLayout);
                            mAdViewLayout.removeView(mAdView);
                            mAdView.destroy();
                            mAdView = null;
                            Log.d(logTag, "Banner AD Removed");
                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                        }
                    });
            }

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    private void updateExistingAdView(AdOptions adOptions) {
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                mAdView.loadAd(adRequest);
            });
    }

    /**
     * Follow iOS method Name:
     * https://developers.google.com/admob/ios/banner?hl=ja
     */
    private void createNewAdView(AdOptions adOptions) {
        // Run AdMob In Main UI Thread
        activitySupplier
            .get()
            .runOnUiThread(() -> {
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
                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(mAdView);

                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                            notifyListeners(BannerAdPluginEvents.Loaded.getWebEventName(), emptyObject);
                            super.onAdLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            if (mAdView != null) {
                                mViewGroup.removeView(mAdViewLayout);
                                mAdViewLayout.removeView(mAdView);
                                mAdView.destroy();
                                mAdView = null;
                            }

                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                            final AdMobPluginError adMobPluginError = new AdMobPluginError(adError);
                            notifyListeners(BannerAdPluginEvents.FailedToLoad.getWebEventName(), adMobPluginError);

                            super.onAdFailedToLoad(adError);
                        }

                        @Override
                        public void onAdOpened() {
                            notifyListeners(BannerAdPluginEvents.Opened.getWebEventName(), emptyObject);
                            super.onAdOpened();
                        }

                        @Override
                        public void onAdClosed() {
                            notifyListeners(BannerAdPluginEvents.Closed.getWebEventName(), emptyObject);
                            super.onAdClosed();
                        }

                        @Override
                        public void onAdImpression() {
                            notifyListeners(BannerAdPluginEvents.AdImpression.getWebEventName(), emptyObject);
                            super.onAdImpression();
                        }
                    }
                );

                // Add AdViewLayout top of the WebView
                mViewGroup.addView(mAdViewLayout);
            });
    }
}
