package com.getcapacitor.community.admob.banner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;
import androidx.core.view.WindowInsetsCompat;
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
import java.util.Objects;

public class BannerExecutor extends Executor {

    private final JSObject emptyObject = new JSObject();
    private RelativeLayout mAdViewLayout;
    private AdView mAdView;
    private boolean bannerLoaded;
    private AdOptions mAdOptions;
    private int[] mBannerMargins;
    private ViewGroup mViewGroup;
    private PreparedBanner preparedBanner;

    private static final class PreparedBanner {

        final AdOptions options;
        PluginCall pending;
        AdView view;
        AdSize size;
        String adUnitID;

        PreparedBanner(AdOptions options, PluginCall pending) {
            this.options = options;
            this.pending = pending;
        }
    }

    public BannerExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "BannerExecutor");
    }

    /**
     * How long to wait for the banner parent to appear before giving up. Deliberately
     * generous: the devices where the parent is late are the slow ones, and a premature
     * timeout now fails AdMob.initialize() outright rather than only the banner.
     */
    private static final long PARENT_TIMEOUT_MS = 5000;

    /**
     * Resolve the banner parent, waiting for the layout pass that adds it when it is not
     * there yet, and report the outcome to `onResult` on the UI thread.
     *
     * android.R.id.content can still have no child when initialize() runs - for example
     * when the app is relaunched quickly after being closed - and the old code cached that
     * null for the lifetime of the process, so every later showBanner() threw
     * NullPointerException on it. Waiting here lets AdMob.initialize() mean what callers
     * already read it as: ready to show a banner.
     */
    public void awaitViewGroup(final Consumer<Boolean> onResult) {
        if (resolveViewGroup() != null) {
            onResult.accept(true);
            return;
        }

        Activity activity = liveActivity();
        View content = activity == null ? null : activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            Log.w(logTag, "Banner parent unavailable: no usable android.R.id.content");
            onResult.accept(false);
            return;
        }

        final View contentView = content;
        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] settled = { false };
        final ViewTreeObserver.OnGlobalLayoutListener[] listener = new ViewTreeObserver.OnGlobalLayoutListener[1];
        final Runnable[] timeout = new Runnable[1];

        listener[0] = () -> {
            if (settled[0] || resolveViewGroup() == null) {
                return;
            }
            settled[0] = true;
            handler.removeCallbacks(timeout[0]);
            removeGlobalLayoutListener(contentView, listener[0]);
            onResult.accept(true);
        };

        timeout[0] = () -> {
            if (settled[0]) {
                return;
            }
            settled[0] = true;
            removeGlobalLayoutListener(contentView, listener[0]);
            Log.w(logTag, "Banner parent never appeared within " + PARENT_TIMEOUT_MS + "ms");
            onResult.accept(false);
        };

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(listener[0]);

        // A child added between the first attempt and registering the listener would not
        // fire it, so try once more now that we are listening.
        if (resolveViewGroup() != null) {
            settled[0] = true;
            removeGlobalLayoutListener(contentView, listener[0]);
            onResult.accept(true);
            return;
        }

        handler.postDelayed(timeout[0], PARENT_TIMEOUT_MS);
    }

    /**
     * The banner's parent, or null while it is unavailable. Never caches a null: the
     * lookup is retried on each use so a later call can succeed once layout has settled.
     */
    private ViewGroup resolveViewGroup() {
        if (mViewGroup != null) {
            return mViewGroup;
        }
        Activity activity = liveActivity();
        if (activity == null) {
            return null;
        }
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return null;
        }
        // Must be content's first child: showBanner builds CoordinatorLayout.LayoutParams,
        // which android.R.id.content (a FrameLayout) rejects with a ClassCastException
        // while measuring.
        View child = ((ViewGroup) content).getChildAt(0);
        if (child instanceof ViewGroup) {
            mViewGroup = (ViewGroup) child;
        }
        return mViewGroup;
    }

    /** The current activity, or null when it is gone or on its way out. */
    private Activity liveActivity() {
        Activity activity = activitySupplier.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return null;
        }
        return activity;
    }

    private static void removeGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnGlobalLayoutListener(listener);
        }
    }

    public void getAdaptiveBannerSize(final PluginCall call) {
        Activity activity = liveActivity();
        if (activity == null) {
            call.reject("Activity unavailable");
            return;
        }
        activity.runOnUiThread(() -> {
            try {
                Double width = call.getDouble("width");
                if (call.getData().has("width") && (width == null || !Double.isFinite(width) || width < 1 || width > Integer.MAX_VALUE)) {
                    call.reject("width must be a finite positive number of logical pixels");
                    return;
                }
                AdSize size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    contextSupplier.get(),
                    width == null ? adaptiveBannerWidth() : width.intValue()
                );
                if (size.getWidth() <= 0 || size.getHeight() <= 0) {
                    call.reject("Unable to calculate an adaptive banner size for this width");
                    return;
                }
                call.resolve(new BannerAdSizeInfo(size.getWidth(), size.getHeight()));
            } catch (Exception ex) {
                call.reject(ex.getLocalizedMessage(), ex);
            }
        });
    }

    private int adaptiveBannerWidth() {
        DisplayMetrics metrics = contextSupplier.get().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        ViewGroup parent = resolveViewGroup();
        if (parent != null && parent.getWidth() > 0) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            int left = location[0] + parent.getPaddingLeft();
            int right = location[0] + parent.getWidth() - parent.getPaddingRight();
            Rect visible = new Rect();
            parent.getWindowVisibleDisplayFrame(visible);
            if (visible.width() > 0) {
                left = Math.max(left, visible.left);
                right = Math.min(right, visible.right);
            }
            width = right - left;
        }
        return (int) (width / metrics.density);
    }

    public void prepareBanner(final PluginCall call) {
        Activity activity = liveActivity();
        if (activity == null) {
            call.reject("Activity unavailable");
            return;
        }
        final PreparedBanner prepared;
        try {
            prepared = new PreparedBanner(AdOptions.getFactory().createBannerOptions(call), call);
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
            return;
        }
        final PreparedBanner previous;
        synchronized (this) {
            previous = preparedBanner;
            preparedBanner = prepared;
        }
        discardPreparedBanner(previous, "Banner preparation was superseded by prepareBanner()", null);
        activity.runOnUiThread(() -> {
            synchronized (BannerExecutor.this) {
                if (preparedBanner != prepared) return;
            }
            try {
                AdSize size = requestedBannerSize(prepared.options);
                if (size == null || size.equals(AdSize.INVALID)) throw new IllegalArgumentException("Invalid banner size");
                prepared.view = new AdView(contextSupplier.get());
                prepared.view.setAdSize(size);
                prepared.size = size;
                AdRequest request = RequestHelper.createRequest(prepared.options);
                AdViewIdHelper.assignIdToAdView(prepared.view, prepared.options, request, logTag, contextSupplier.get());
                prepared.adUnitID = prepared.view.getAdUnitId();
                prepared.view.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            PluginCall pending;
                            synchronized (BannerExecutor.this) {
                                if (preparedBanner != prepared || prepared.pending == null) return;
                                pending = prepared.pending;
                                prepared.pending = null;
                            }
                            notifyListeners(BannerAdPluginEvents.Loaded.getWebEventName(), emptyObject);
                            pending.resolve();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError error) {
                            if (!detachPreparedBanner(prepared)) return;
                            discardPreparedBanner(prepared, error.getMessage(), String.valueOf(error.getCode()));
                            notifyListeners(BannerAdPluginEvents.FailedToLoad.getWebEventName(), new AdMobPluginError(error));
                        }
                    }
                );
                prepared.view.loadAd(request);
            } catch (Exception ex) {
                if (detachPreparedBanner(prepared)) discardPreparedBanner(prepared, ex.getLocalizedMessage(), null);
            }
        });
    }

    private synchronized PreparedBanner takePreparedBanner() {
        PreparedBanner prepared = preparedBanner;
        preparedBanner = null;
        return prepared;
    }

    private synchronized boolean detachPreparedBanner(PreparedBanner prepared) {
        if (preparedBanner != prepared) return false;
        preparedBanner = null;
        return true;
    }

    private void discardPreparedBanner(PreparedBanner prepared, String reason, String code) {
        if (prepared == null) return;
        PluginCall pending = prepared.pending;
        prepared.pending = null;
        if (pending != null) pending.reject(reason, code);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (prepared.view != null) prepared.view.destroy();
        });
    }

    private AdSize requestedBannerSize(AdOptions options) {
        return options.adSize.toString().equals("ADAPTIVE_BANNER")
            ? AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(contextSupplier.get(), adaptiveBannerWidth())
            : options.adSize.getSize();
    }

    private boolean canUsePreparedBanner(PreparedBanner prepared, AdOptions options, AdSize size) {
        return (
            prepared.pending == null &&
            prepared.view != null &&
            prepared.options.adSize == options.adSize &&
            prepared.options.npa == options.npa &&
            prepared.options.isTesting == options.isTesting &&
            Objects.equals(prepared.size, size) &&
            Objects.equals(
                prepared.adUnitID,
                AdViewIdHelper.getFinalAdId(options, RequestHelper.createRequest(options), logTag, contextSupplier.get())
            )
        );
    }

    public void getBannerSize(final PluginCall call) {
        activitySupplier.get().runOnUiThread(() -> {
            boolean visible = bannerLoaded && mAdView != null && mAdViewLayout.getVisibility() == View.VISIBLE;
            call.resolve(visible ? new BannerAdSizeInfo(mAdView) : new BannerAdSizeInfo(0, 0));
        });
    }

    public void showBanner(final PluginCall call, boolean systemBarsHandlesInsets) {
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

        PreparedBanner prepared = takePreparedBanner();
        boolean usePrepared = false;
        if (prepared != null) {
            try {
                usePrepared = canUsePreparedBanner(prepared, adOptions, requestedBannerSize(adOptions));
            } catch (Exception ex) {
                discardPreparedBanner(prepared, ex.getLocalizedMessage(), null);
                call.reject(ex.getLocalizedMessage(), ex);
                return;
            }
            if (!usePrepared) discardPreparedBanner(prepared, "Banner preparation was superseded by showBanner()", null);
        }
        if (mAdView != null && !usePrepared) {
            updateExistingAdView(call, adOptions);
            return;
        }

        final AdView previousAdView = mAdView;
        final RelativeLayout previousLayout = mAdViewLayout;
        final boolean previousLoaded = bannerLoaded;
        // Why a try catch block?
        try {
            bannerLoaded = false;
            mAdView = usePrepared ? prepared.view : new AdView(contextSupplier.get());
            if (!usePrepared) mAdView.setAdSize(requestedBannerSize(adOptions));

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
            // Apply the requested offset only at the anchored edge. Applying it to
            // both edges reduces the available height twice and clips large offsets.
            int topMargin = "TOP_CENTER".equals(adOptions.position) ? densityMargin : 0;
            int bottomMargin = "BOTTOM_CENTER".equals(adOptions.position) ? densityMargin : 0;
            int[] margins = new int[] { 0, topMargin, 0, bottomMargin };
            mBannerMargins = margins;

            // Center Banner Ads
            int adWidth = (int) (adOptions.adSize.getSize().getWidth() * density);

            if (adWidth <= 0 || adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                int margin = 0;
                if (fullscreen) {
                    margin = (realWidthPixels - defaultWidthPixels) / 2;
                }
                margins[0] = margin;
                margins[2] = margin;
                mAdViewLayoutParams.setMargins(margin, topMargin, margin, bottomMargin);
            } else {
                int sideMargin = ((int) defaultWidthPixels - adWidth) / 2;
                if (fullscreen) {
                    sideMargin = (realWidthPixels - adWidth) / 2;
                }
                margins[0] = sideMargin;
                margins[2] = sideMargin;
                mAdViewLayoutParams.setMargins(sideMargin, topMargin, sideMargin, bottomMargin);
            }

            // Use the insets remaining after Capacitor's parent layout has handled them.
            // Replacing the decor-view listener would override SystemBars' safe-area handling.
            mAdViewLayout.setOnApplyWindowInsetsListener((v, insets) -> {
                WindowInsetsCompat compat = WindowInsetsCompat.toWindowInsetsCompat(insets, v);
                Insets safeArea = compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                boolean keyboardVisible =
                    compat.isVisible(WindowInsetsCompat.Type.ime()) && compat.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0;
                // SystemBars already pads the parent for the keyboard. Neither the IME
                // height nor the navigation bar belongs in the banner's margin then.
                // With insetsHandling=disable, the host app owns keyboard avoidance.
                int bottomInset = keyboardVisible && systemBarsHandlesInsets ? 0 : safeArea.bottom;

                if ((mAdViewLayoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
                    mAdViewLayoutParams.setMargins(margins[0], margins[1] + safeArea.top, margins[2], margins[3]);
                } else {
                    mAdViewLayoutParams.setMargins(margins[0], margins[1], margins[2], margins[3] + bottomInset);
                }

                v.setLayoutParams(mAdViewLayoutParams);
                return insets;
            });

            mAdOptions = adOptions;
            createNewAdView(adOptions, usePrepared, previousAdView, previousLayout, previousLoaded);

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
            activitySupplier.get().runOnUiThread(() -> {
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
            activitySupplier.get().runOnUiThread(() -> {
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
        discardPreparedBanner(takePreparedBanner(), "Banner preparation was cancelled by removeBanner()", null);
        try {
            if (mAdView != null) {
                activitySupplier.get().runOnUiThread(() -> {
                    if (mAdView != null) {
                        final ViewGroup bannerParent = resolveViewGroup();
                        if (bannerParent != null) {
                            bannerParent.removeView(mAdViewLayout);
                        }
                        mAdViewLayout.removeView(mAdView);
                        mAdView.destroy();
                        mAdView = null;
                        bannerLoaded = false;
                        mAdOptions = null;
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

    private void updateExistingAdView(PluginCall call, AdOptions adOptions) {
        // Bind to the AdView present when this call was made. `mAdView` is a
        // shared field that another UI-thread task can null before this one
        // runs; using the captured reference avoids a NullPointerException.
        final AdView adView = mAdView;
        try {
            activitySupplier.get().runOnUiThread(() -> {
                if (adView != mAdView) {
                    call.reject("Banner was removed or replaced before the update");
                    return;
                }
                try {
                    boolean layoutOnly =
                        mAdOptions != null &&
                        (mAdOptions.margin != adOptions.margin || !Objects.equals(mAdOptions.position, adOptions.position)) &&
                        Objects.equals(mAdOptions.adId, adOptions.adId) &&
                        mAdOptions.isTesting == adOptions.isTesting &&
                        mAdOptions.npa == adOptions.npa;
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAdViewLayout.getLayoutParams();
                    int offset = (int) (adOptions.margin * contextSupplier.get().getResources().getDisplayMetrics().density);
                    params.gravity =
                        ("TOP_CENTER".equals(adOptions.position)
                            ? Gravity.TOP
                            : "CENTER".equals(adOptions.position)
                              ? Gravity.CENTER
                              : Gravity.BOTTOM) | Gravity.CENTER_HORIZONTAL;
                    // Keep raw margins separate so repeated inset passes never accumulate them.
                    mBannerMargins[0] = mBannerMargins[2] = 0;
                    mBannerMargins[1] = "TOP_CENTER".equals(adOptions.position) ? offset : 0;
                    mBannerMargins[3] = "BOTTOM_CENTER".equals(adOptions.position) ? offset : 0;
                    params.setMargins(mBannerMargins[0], mBannerMargins[1], mBannerMargins[2], mBannerMargins[3]);
                    mAdViewLayout.setLayoutParams(params);
                    mAdViewLayout.requestApplyInsets();
                    if (!layoutOnly) adView.loadAd(RequestHelper.createRequest(adOptions));
                    if (adView == mAdView) mAdOptions = adOptions;
                    call.resolve();
                } catch (Exception ex) {
                    call.reject(ex.getLocalizedMessage(), ex);
                }
            });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Follow iOS method Name:
     * https://developers.google.com/admob/ios/banner?hl=ja
     */
    private void createNewAdView(
        AdOptions adOptions,
        boolean usePrepared,
        AdView previousAdView,
        RelativeLayout previousLayout,
        boolean previousLoaded
    ) {
        // Bind to the AdView instance created for this call. `mAdView` is a
        // shared field that removeBanner/hideBanner or a stale ad-listener
        // callback can null from the UI thread before this posted task runs;
        // reading the field inside the task would then throw a
        // NullPointerException (e.g. in AdViewIdHelper.assignIdToAdView).
        final AdView adView = mAdView;

        // Run AdMob In Main UI Thread
        activitySupplier.get().runOnUiThread(() -> {
            BannerAdSizeInfo previousSize =
                previousLoaded && previousLayout.getVisibility() == View.VISIBLE && previousLayout.getParent() != null
                    ? new BannerAdSizeInfo(previousAdView)
                    : null;
            // A ready preparation replaces the displayed banner only when show is called.
            if (previousAdView != null) {
                ViewGroup parent = (ViewGroup) previousLayout.getParent();
                if (parent != null) parent.removeView(previousLayout);
                previousLayout.removeView(previousAdView);
                previousAdView.setAdListener(null);
                previousAdView.setOnPaidEventListener(null);
                previousAdView.destroy();
            }
            if (adView != mAdView) {
                // Banner was removed or replaced before this task ran.
                return;
            }
            final AdRequest adRequest = usePrepared ? null : RequestHelper.createRequest(adOptions);
            // A prepared view already has its size, unit ID, and loaded request.
            if (!usePrepared) AdViewIdHelper.assignIdToAdView(adView, adOptions, adRequest, logTag, contextSupplier.get());
            // Add the AdView to the view hierarchy.
            mAdViewLayout.addView(adView);
            // Start loading the ad.
            if (!usePrepared) adView.loadAd(adRequest);
            adView.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (adView != mAdView) {
                            return;
                        }
                        bannerLoaded = true;
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

                        final ViewGroup bannerParent = resolveViewGroup();
                        if (bannerParent != null) {
                            bannerParent.removeView(mAdViewLayout);
                        }
                        mAdViewLayout.removeView(adView);
                        adView.destroy();
                        mAdView = null;
                        bannerLoaded = false;
                        mAdOptions = null;

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
            final ViewGroup bannerParent = resolveViewGroup();
            if (bannerParent != null) {
                bannerParent.addView(mAdViewLayout);
                mAdViewLayout.requestApplyInsets();
                if (usePrepared) {
                    bannerLoaded = true;
                    BannerAdSizeInfo size = new BannerAdSizeInfo(adView);
                    if (!size.equals(previousSize)) notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), size);
                }
            } else {
                Log.w(logTag, "Banner not attached: parent unavailable");
            }
        });
    }
}
