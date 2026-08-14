package com.getcapacitor.community.admob.banner;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
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
import com.getcapacitor.community.admob.models.AdMobRevenueData;
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
            int[] margins = new int[] { 0, densityMargin, 0, densityMargin };

            // Center Banner Ads
            int adWidth = (int) (adOptions.adSize.getSize().getWidth() * density);

            if (adWidth <= 0 || adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                int margin = 0;
                if (fullscreen) {
                    margin = (realWidthPixels - defaultWidthPixels) / 2;
                }
                margins[0] = margin;
                margins[2] = margin;
                mAdViewLayoutParams.setMargins(margin, densityMargin, margin, densityMargin);
            } else {
                int sideMargin = ((int) defaultWidthPixels - adWidth) / 2;
                if (fullscreen) {
                    sideMargin = (realWidthPixels - adWidth) / 2;
                }
                margins[0] = sideMargin;
                margins[2] = sideMargin;
                mAdViewLayoutParams.setMargins(sideMargin, densityMargin, sideMargin, densityMargin);
            }

            // set Safe Area only for Android 15+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                View rootView = activitySupplier.get().getWindow().getDecorView();
                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                    int bottomInset = insets.getSystemWindowInsetBottom();
                    int topInset = insets.getSystemWindowInsetTop();

                    if ("TOP_CENTER".equals(adOptions.position)) {
                        mAdViewLayoutParams.setMargins(margins[0], margins[1] + topInset, margins[2], margins[3]);
                    } else {
                        mAdViewLayoutParams.setMargins(margins[0], margins[1], margins[2], margins[3] + bottomInset);
                    }

                    mAdViewLayout.setLayoutParams(mAdViewLayoutParams);
                    return insets;
                });
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
        // Bind to the AdView present when this call was made. `mAdView` is a
        // shared field that another UI-thread task can null before this one
        // runs; using the captured reference avoids a NullPointerException.
        final AdView adView = mAdView;
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (adView != mAdView) {
                    // Banner was removed or replaced before this task ran.
                    return;
                }
                final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                adView.loadAd(adRequest);
            });
    }

    /**
     * Follow iOS method Name:
     * https://developers.google.com/admob/ios/banner?hl=ja
     */
    private void createNewAdView(AdOptions adOptions) {
        // Bind to the AdView instance created for this call. `mAdView` is a
        // shared field that removeBanner/hideBanner or a stale ad-listener
        // callback can null from the UI thread before this posted task runs;
        // reading the field inside the task would then throw a
        // NullPointerException (e.g. in AdViewIdHelper.assignIdToAdView).
        final AdView adView = mAdView;

        // Run AdMob In Main UI Thread
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (adView != mAdView) {
                    // Banner was removed or replaced before this task ran.
                    return;
                }
                final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                // Assign the correct id needed
                AdViewIdHelper.assignIdToAdView(adView, adOptions, adRequest, logTag, contextSupplier.get());
                // Add the AdView to the view hierarchy.
                mAdViewLayout.addView(adView);
                // Start loading the ad.
                adView.loadAd(adRequest);
                adView.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            if (adView != mAdView) {
                                return;
                            }
                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(adView);

                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                            notifyListeners(BannerAdPluginEvents.Loaded.getWebEventName(), emptyObject);
                            super.onAdLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            if (adView != mAdView) {
                                // Stale callback from a banner that was already removed or
                                // replaced. Do not touch the current banner or emit teardown
                                // events for a view the JS layer has already discarded.
                                super.onAdFailedToLoad(adError);
                                return;
                            }

                            mViewGroup.removeView(mAdViewLayout);
                            mAdViewLayout.removeView(adView);
                            adView.destroy();
                            mAdView = null;

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

                adView.setOnPaidEventListener((adValue) -> {
                    if (adView != mAdView) {
                        return;
                    }
                    String networkName = "";
                    String impressionId = "";
                    if (adView.getResponseInfo() != null) {
                        networkName = adView.getResponseInfo().getMediationAdapterClassName();
                        if (networkName == null) networkName = "";
                        impressionId = adView.getResponseInfo().getResponseId();
                        if (impressionId == null) impressionId = "";
                    }
                    AdMobRevenueData revenueData = new AdMobRevenueData(adValue, adView.getAdUnitId(), networkName, impressionId);
                    notifyListeners(BannerAdPluginEvents.AdPaid.getWebEventName(), revenueData);
                });

                // Add AdViewLayout top of the WebView
                mViewGroup.addView(mAdViewLayout);
            });
    }
}
