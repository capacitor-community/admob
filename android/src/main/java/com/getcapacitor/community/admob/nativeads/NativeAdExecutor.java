package com.getcapacitor.community.admob.nativeads;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobRevenueData;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.common.util.BiConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeAdExecutor extends Executor {

    public static final String NATIVE_TESTER_ID = "ca-app-pub-3940256099942544/2247696110";
    private static final int MAX_ADS_PER_FEED = 3;

    private static final class NativeAdState {

        final String feedId;
        final boolean isSmall;
        final Object loadToken;
        final NativeAd nativeAd;
        final NativeAdView adView;
        final FrameLayout clippingContainer;
        int generation = -1;

        NativeAdState(
            String feedId,
            boolean isSmall,
            Object loadToken,
            NativeAd nativeAd,
            NativeAdView adView,
            FrameLayout clippingContainer
        ) {
            this.feedId = feedId;
            this.isSmall = isSmall;
            this.loadToken = loadToken;
            this.nativeAd = nativeAd;
            this.adView = adView;
            this.clippingContainer = clippingContainer;
        }
    }

    private static final class PendingLoad {

        final PluginCall call;
        final String feedId;
        final Object token = new Object();

        PendingLoad(PluginCall call, String feedId) {
            this.call = call;
            this.feedId = feedId;
        }
    }

    private final Supplier<View> webViewSupplier;
    private final Map<String, NativeAdState> states = new HashMap<>();
    private final Map<String, PendingLoad> pendingLoads = new HashMap<>();
    private final NativeAdFeedSessions feedSessions = new NativeAdFeedSessions();

    public NativeAdExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        Supplier<View> webViewSupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "NativeAdExecutor");
        this.webViewSupplier = webViewSupplier;
    }

    public void startFeed(final PluginCall call) {
        final String feedId = requiredString(call, "feedId");
        final String sessionId = requiredString(call, "sessionId");
        if (feedId == null || sessionId == null) return;
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (!feedSessions.isCurrent(feedId, sessionId)) clearFeed(feedId);
                if (!feedSessions.start(feedId, sessionId)) {
                    call.reject("At most two native ad feeds can be active");
                    return;
                }
                call.resolve();
            });
    }

    public void destroyFeed(final PluginCall call) {
        final String feedId = requiredString(call, "feedId");
        final String sessionId = requiredString(call, "sessionId");
        if (feedId == null || sessionId == null) return;
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (feedSessions.isCurrent(feedId, sessionId)) {
                    clearFeed(feedId);
                    feedSessions.remove(feedId);
                }
                call.resolve();
            });
    }

    public void load(final PluginCall call) {
        final String feedId = requiredString(call, "feedId");
        final String sessionId = requiredString(call, "sessionId");
        final String slotKey = requiredString(call, "slotKey");
        if (feedId == null || sessionId == null || slotKey == null) return;

        final AdOptions options = AdOptions.getFactory().createGenericOptions(call, NATIVE_TESTER_ID);
        final String template = call.getString("template", "medium");
        final JSObject style = call.getObject("style", new JSObject());
        final String stateKey = stateKey(feedId, slotKey);
        final AtomicBoolean finished = new AtomicBoolean(false);
        final PendingLoad pendingLoad = new PendingLoad(call, feedId);

        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (!isCurrentSession(feedId, sessionId)) {
                    call.reject("Native ad feed session is no longer active");
                    return;
                }
                destroyState(stateKey);
                cancelPendingLoad(stateKey);
                long feedAdCount = states
                    .values()
                    .stream()
                    .filter((state) -> state.feedId.equals(feedId))
                    .count();
                feedAdCount += pendingLoads
                    .values()
                    .stream()
                    .filter((pending) -> pending.feedId.equals(feedId))
                    .count();
                if (feedAdCount >= MAX_ADS_PER_FEED) {
                    call.reject("At most three native ads can be active in a feed");
                    return;
                }
                pendingLoads.put(stateKey, pendingLoad);
                final AdLoader adLoader = new AdLoader.Builder(contextSupplier.get(), NATIVE_TESTER_ID)
                    .forNativeAd((nativeAd) -> {
                        if (pendingLoads.get(stateKey) != pendingLoad || !finished.compareAndSet(false, true)) {
                            nativeAd.destroy();
                            return;
                        }
                        pendingLoads.remove(stateKey);
                        NativeAdView adView = PluginNativeAdView.create(contextSupplier.get(), nativeAd, template, style);
                        FrameLayout clippingContainer = new FrameLayout(contextSupplier.get());
                        clippingContainer.setClipChildren(true);
                        clippingContainer.setClipToPadding(true);
                        clippingContainer.setVisibility(View.GONE);
                        clippingContainer.addView(adView);

                        ViewGroup overlayParent = getOverlayParent();
                        if (overlayParent == null) {
                            adView.destroy();
                            nativeAd.destroy();
                            call.reject("AdMob WebView parent is unavailable");
                            return;
                        }

                        NativeAdState state = new NativeAdState(
                            feedId,
                            "small".equals(template),
                            pendingLoad.token,
                            nativeAd,
                            adView,
                            clippingContainer
                        );
                        states.put(stateKey, state);
                        overlayParent.addView(clippingContainer);

                        nativeAd.setOnPaidEventListener((adValue) -> {
                            if (states.get(stateKey) != state) return;
                            String networkName = "";
                            String impressionId = "";
                            if (nativeAd.getResponseInfo() != null) {
                                networkName = nativeAd.getResponseInfo().getMediationAdapterClassName();
                                if (networkName == null) networkName = "";
                                impressionId = nativeAd.getResponseInfo().getResponseId();
                                if (impressionId == null) impressionId = "";
                            }
                            AdMobRevenueData data = new AdMobRevenueData(adValue, NATIVE_TESTER_ID, networkName, impressionId);
                            data.put("feedId", feedId);
                            data.put("slotKey", slotKey);
                            data.put("sessionId", sessionId);
                            notifyListeners(NativeAdPluginEvents.AD_PAID.getWebEventName(), data);
                        });

                        notifyListeners(NativeAdPluginEvents.LOADED.getWebEventName(), identity(feedId, sessionId, slotKey));
                        call.resolve();
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
                    .withAdListener(
                        new AdListener() {
                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                                if (pendingLoads.get(stateKey) != pendingLoad) return;
                                pendingLoads.remove(stateKey);
                                JSObject event = identity(feedId, sessionId, slotKey);
                                event.put("code", error.getCode());
                                event.put("message", error.getMessage());
                                notifyListeners(NativeAdPluginEvents.FAILED_TO_LOAD.getWebEventName(), event);
                                if (finished.compareAndSet(false, true)) {
                                    call.reject(error.getMessage(), String.valueOf(error.getCode()), event);
                                }
                            }

                            @Override
                            public void onAdClicked() {
                                if (!isCurrent(stateKey, pendingLoad.token)) return;
                                notifyListeners(NativeAdPluginEvents.CLICKED.getWebEventName(), identity(feedId, sessionId, slotKey));
                            }

                            @Override
                            public void onAdImpression() {
                                if (!isCurrent(stateKey, pendingLoad.token)) return;
                                notifyListeners(NativeAdPluginEvents.IMPRESSION.getWebEventName(), identity(feedId, sessionId, slotKey));
                            }

                            @Override
                            public void onAdOpened() {
                                if (!isCurrent(stateKey, pendingLoad.token)) return;
                                notifyListeners(NativeAdPluginEvents.OPENED.getWebEventName(), identity(feedId, sessionId, slotKey));
                            }

                            @Override
                            public void onAdClosed() {
                                if (!isCurrent(stateKey, pendingLoad.token)) return;
                                notifyListeners(NativeAdPluginEvents.CLOSED.getWebEventName(), identity(feedId, sessionId, slotKey));
                            }
                        }
                    )
                    .build();

                adLoader.loadAd(RequestHelper.createRequest(options));
            });
    }

    public void updatePlacements(final PluginCall call) {
        final String feedId = requiredString(call, "feedId");
        final String sessionId = requiredString(call, "sessionId");
        if (feedId == null || sessionId == null) return;
        final long sequence = call.getLong("sequence", -1L);
        final JSArray placements = call.getArray("placements", new JSArray());
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (!isCurrentSession(feedId, sessionId)) {
                    call.resolve();
                    return;
                }
                if (!feedSessions.accepts(feedId, sequence)) {
                    call.resolve();
                    return;
                }
                for (NativeAdState state : states.values()) {
                    if (state.feedId.equals(feedId)) state.clippingContainer.setVisibility(View.GONE);
                }
                for (int index = 0; index < placements.length(); index++) {
                    try {
                        applyPlacement(feedId, placements.getJSONObject(index));
                    } catch (JSONException ignored) {
                        // Invalid entries fail closed: every state was hidden above.
                    }
                }
                call.resolve();
            });
    }

    public void remove(final PluginCall call) {
        final String feedId = requiredString(call, "feedId");
        final String sessionId = requiredString(call, "sessionId");
        final String slotKey = requiredString(call, "slotKey");
        if (feedId == null || sessionId == null || slotKey == null) return;
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                if (!isCurrentSession(feedId, sessionId)) {
                    call.resolve();
                    return;
                }
                cancelPendingLoad(stateKey(feedId, slotKey));
                destroyState(stateKey(feedId, slotKey));
                call.resolve();
            });
    }

    public void destroyAll() {
        for (String key : pendingLoads.keySet().toArray(new String[0])) cancelPendingLoad(key);
        for (String key : states.keySet().toArray(new String[0])) destroyState(key);
        feedSessions.clear();
    }

    private void applyPlacement(String expectedFeedId, JSONObject placement) throws JSONException {
        float density = contextSupplier.get().getResources().getDisplayMetrics().density;
        NativeAdPlacementValue value = NativeAdPlacementValue.parse(expectedFeedId, placement, density);
        if (value == null) return;
        NativeAdState state = states.get(stateKey(value.feedId, value.slotKey));
        if (state == null || value.generation < state.generation) return;
        int minimumWidth = Math.round((state.isSmall ? 120 : 144) * density);
        int minimumHeight = Math.round((state.isSmall ? 120 : 300) * density);
        if (value.rectWidth < minimumWidth || value.rectHeight < minimumHeight) return;

        View webView = webViewSupplier.get();
        if (webView == null) return;
        ViewGroup.LayoutParams layoutParams = state.clippingContainer.getLayoutParams();
        layoutParams.width = value.clipWidth;
        layoutParams.height = value.clipHeight;
        state.clippingContainer.setLayoutParams(layoutParams);
        state.clippingContainer.setX(webView.getX() + value.clipX);
        state.clippingContainer.setY(webView.getY() + value.clipY);

        state.adView.setLayoutParams(new FrameLayout.LayoutParams(value.rectWidth, value.rectHeight));
        state.adView.setX(value.rectX - value.clipX);
        state.adView.setY(value.rectY - value.clipY);
        state.generation = value.generation;
        state.clippingContainer.setVisibility(View.VISIBLE);
        state.clippingContainer.bringToFront();
    }

    private ViewGroup getOverlayParent() {
        View webView = webViewSupplier.get();
        return webView != null && webView.getParent() instanceof ViewGroup ? (ViewGroup) webView.getParent() : null;
    }

    private void destroyState(String stateKey) {
        NativeAdState state = states.remove(stateKey);
        if (state == null) return;
        ViewGroup parent = (ViewGroup) state.clippingContainer.getParent();
        if (parent != null) parent.removeView(state.clippingContainer);
        state.adView.destroy();
        state.nativeAd.destroy();
    }

    private void cancelPendingLoad(String stateKey) {
        PendingLoad pendingLoad = pendingLoads.remove(stateKey);
        if (pendingLoad != null) pendingLoad.call.reject("Native ad load was cancelled");
    }

    private boolean isCurrent(String stateKey, Object loadToken) {
        NativeAdState state = states.get(stateKey);
        return state != null && state.loadToken == loadToken;
    }

    private boolean isCurrentSession(String feedId, String sessionId) {
        return feedSessions.isCurrent(feedId, sessionId);
    }

    private void clearFeed(String feedId) {
        for (String key : pendingLoads.keySet().toArray(new String[0])) {
            PendingLoad pendingLoad = pendingLoads.get(key);
            if (pendingLoad != null && pendingLoad.feedId.equals(feedId)) cancelPendingLoad(key);
        }
        for (String key : states.keySet().toArray(new String[0])) {
            NativeAdState state = states.get(key);
            if (state != null && state.feedId.equals(feedId)) destroyState(key);
        }
    }

    private String requiredString(PluginCall call, String name) {
        String value = call.getString(name);
        if (value == null || value.trim().isEmpty()) {
            call.reject(name + " must not be empty");
            return null;
        }
        return value.trim();
    }

    private String stateKey(String feedId, String slotKey) {
        return feedId + "\u0000" + slotKey;
    }

    private JSObject identity(String feedId, String sessionId, String slotKey) {
        JSObject object = new JSObject();
        object.put("feedId", feedId);
        object.put("sessionId", sessionId);
        object.put("slotKey", slotKey);
        return object;
    }
}
