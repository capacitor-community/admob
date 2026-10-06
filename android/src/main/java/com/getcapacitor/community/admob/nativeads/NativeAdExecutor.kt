package com.getcapacitor.community.admob.nativeads

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.util.Supplier
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.RequestHelper
import com.getcapacitor.community.admob.models.AdMobRevenueData
import com.getcapacitor.community.admob.models.AdOptions
import com.getcapacitor.community.admob.models.Executor
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.common.util.BiConsumer
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class NativeAdExecutor(
    contextSupplier: Supplier<Context>,
    activitySupplier: Supplier<Activity>,
    private val webViewSupplier: Supplier<View>,
    notifyListenersFunction: BiConsumer<String, JSObject>,
    pluginLogTag: String,
) : Executor(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "NativeAdExecutor") {
    private data class NativeAdState(
        val feedId: String,
        val isSmall: Boolean,
        val loadToken: Any,
        val nativeAd: NativeAd,
        val adView: NativeAdView,
        val clippingContainer: FrameLayout,
        var generation: Int = -1,
    )

    private data class PendingLoad(
        val call: PluginCall,
        val feedId: String,
        val token: Any = Any(),
    )

    private val states = mutableMapOf<String, NativeAdState>()
    private val pendingLoads = mutableMapOf<String, PendingLoad>()
    private val feedSessions = NativeAdFeedSessions()

    fun startFeed(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        activitySupplier.get().runOnUiThread {
            if (!feedSessions.isCurrent(feedId, sessionId)) clearFeed(feedId)
            if (!feedSessions.start(feedId, sessionId)) {
                call.reject("At most two native ad feeds can be active")
                return@runOnUiThread
            }
            call.resolve()
        }
    }

    fun destroyFeed(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        activitySupplier.get().runOnUiThread {
            if (feedSessions.isCurrent(feedId, sessionId)) {
                clearFeed(feedId)
                feedSessions.remove(feedId)
            }
            call.resolve()
        }
    }

    fun load(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        val slotKey = requiredString(call, "slotKey") ?: return
        val options = AdOptions.getFactory().createGenericOptions(call, NATIVE_TESTER_ID)
        val adId = if (options.isTesting) NATIVE_TESTER_ID else requiredString(call, "adId") ?: return
        val template = call.getString("template", "medium") ?: "medium"
        val style = call.getObject("style", JSObject()) ?: JSObject()
        val stateKey = stateKey(feedId, slotKey)
        val finished = AtomicBoolean(false)
        val pendingLoad = PendingLoad(call, feedId)

        activitySupplier.get().runOnUiThread {
            if (!isCurrentSession(feedId, sessionId)) {
                call.reject("Native ad feed session is no longer active")
                return@runOnUiThread
            }
            destroyState(stateKey)
            cancelPendingLoad(stateKey)
            val feedAdCount = states.values.count { it.feedId == feedId } + pendingLoads.values.count { it.feedId == feedId }
            if (feedAdCount >= MAX_ADS_PER_FEED) {
                call.reject("At most three native ads can be active in a feed")
                return@runOnUiThread
            }
            pendingLoads[stateKey] = pendingLoad
            val adLoader = AdLoader.Builder(contextSupplier.get(), adId)
                .forNativeAd { nativeAd ->
                    handleLoadedAd(
                        nativeAd = nativeAd,
                        adId = adId,
                        stateKey = stateKey,
                        pendingLoad = pendingLoad,
                        finished = finished,
                        feedId = feedId,
                        sessionId = sessionId,
                        slotKey = slotKey,
                        template = template,
                        style = style,
                    )
                }
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build(),
                )
                .withAdListener(adListener(stateKey, pendingLoad, finished, feedId, sessionId, slotKey))
                .build()

            adLoader.loadAd(RequestHelper.createRequest(options))
        }
    }

    // WebMessageListener delivers on the UI thread; lifecycle calls still use PluginCall.
    fun updateDirectPlacements(raw: String?) {
        try {
            applyPlacements(JSONObject(raw ?: return))
        } catch (_: JSONException) {
            // Ignore malformed messages without affecting another feed.
        }
    }

    fun updatePlacements(call: PluginCall) {
        requiredString(call, "feedId") ?: return
        requiredString(call, "sessionId") ?: return
        activitySupplier.get().runOnUiThread {
            applyPlacements(call.data)
            call.resolve()
        }
    }

    private fun applyPlacements(batch: JSONObject) {
        val feedId = batch.optString("feedId")
        val sessionId = batch.optString("sessionId")
        if (!isCurrentSession(feedId, sessionId) || !feedSessions.accepts(feedId, batch.optLong("sequence", -1L))) return
        val shown = mutableSetOf<String>()
        val placements = batch.optJSONArray("placements") ?: JSArray()
        repeat(placements.length()) { index ->
            try {
                applyPlacement(feedId, placements.getJSONObject(index))?.let { shown.add(it) }
            } catch (_: JSONException) {
                // Invalid entries fail closed below.
            }
        }
        states.filter { it.value.feedId == feedId && it.key !in shown }.values.forEach {
            it.clippingContainer.visibility = View.GONE
        }
    }

    fun remove(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        val slotKey = requiredString(call, "slotKey") ?: return
        activitySupplier.get().runOnUiThread {
            if (isCurrentSession(feedId, sessionId)) {
                val key = stateKey(feedId, slotKey)
                cancelPendingLoad(key)
                destroyState(key)
            }
            call.resolve()
        }
    }

    fun destroyAll() {
        pendingLoads.keys.toList().forEach(::cancelPendingLoad)
        states.keys.toList().forEach(::destroyState)
        feedSessions.clear()
    }

    private fun handleLoadedAd(
        nativeAd: NativeAd,
        adId: String,
        stateKey: String,
        pendingLoad: PendingLoad,
        finished: AtomicBoolean,
        feedId: String,
        sessionId: String,
        slotKey: String,
        template: String,
        style: JSObject,
    ) {
        if (pendingLoads[stateKey] !== pendingLoad || !finished.compareAndSet(false, true)) {
            nativeAd.destroy()
            return
        }
        pendingLoads.remove(stateKey)
        if (template == "small" && nativeAd.mediaContent?.hasVideoContent() == true) {
            nativeAd.destroy()
            val message = "Small native ad template does not support video ads; use Medium"
            val event = identity(feedId, sessionId, slotKey).apply {
                put("code", -1)
                put("message", message)
            }
            notifyListeners(NativeAdPluginEvents.FAILED_TO_LOAD, event)
            pendingLoad.call.reject(message, "-1", event)
            return
        }
        val adView = PluginNativeAdView.create(contextSupplier.get(), nativeAd, template, style)
        val clippingContainer = FrameLayout(contextSupplier.get()).apply {
            clipChildren = true
            clipToPadding = true
            visibility = View.GONE
            addView(adView)
        }
        val overlayParent = overlayParent()
        if (overlayParent == null) {
            adView.destroy()
            nativeAd.destroy()
            pendingLoad.call.reject("AdMob WebView parent is unavailable")
            return
        }

        val state = NativeAdState(feedId, template == "small", pendingLoad.token, nativeAd, adView, clippingContainer)
        states[stateKey] = state
        overlayParent.addView(clippingContainer)
        nativeAd.setOnPaidEventListener { adValue ->
            if (states[stateKey] !== state) return@setOnPaidEventListener
            val responseInfo = nativeAd.responseInfo
            val data = AdMobRevenueData(
                adValue,
                adId,
                responseInfo?.mediationAdapterClassName ?: "",
                responseInfo?.responseId ?: "",
            ).apply {
                put("feedId", feedId)
                put("slotKey", slotKey)
                put("sessionId", sessionId)
            }
            notifyListeners(NativeAdPluginEvents.AD_PAID, data)
        }

        notifyListeners(NativeAdPluginEvents.LOADED, identity(feedId, sessionId, slotKey))
        pendingLoad.call.resolve()
    }

    private fun adListener(
        stateKey: String,
        pendingLoad: PendingLoad,
        finished: AtomicBoolean,
        feedId: String,
        sessionId: String,
        slotKey: String,
    ): AdListener = object : AdListener() {
        override fun onAdFailedToLoad(error: LoadAdError) {
            if (pendingLoads[stateKey] !== pendingLoad) return
            pendingLoads.remove(stateKey)
            val event = identity(feedId, sessionId, slotKey).apply {
                put("code", error.code)
                put("message", error.message)
            }
            notifyListeners(NativeAdPluginEvents.FAILED_TO_LOAD, event)
            if (finished.compareAndSet(false, true)) {
                pendingLoad.call.reject(error.message, error.code.toString(), event)
            }
        }

        override fun onAdClicked() = notifyIfCurrent(NativeAdPluginEvents.CLICKED)

        override fun onAdImpression() = notifyIfCurrent(NativeAdPluginEvents.IMPRESSION)

        override fun onAdOpened() = notifyIfCurrent(NativeAdPluginEvents.OPENED)

        override fun onAdClosed() = notifyIfCurrent(NativeAdPluginEvents.CLOSED)

        private fun notifyIfCurrent(eventName: String) {
            if (isCurrent(stateKey, pendingLoad.token)) {
                notifyListeners(eventName, identity(feedId, sessionId, slotKey))
            }
        }
    }

    private fun applyPlacement(expectedFeedId: String, placement: JSONObject): String? {
        val density = contextSupplier.get().resources.displayMetrics.density
        val value = NativeAdPlacementValue.parse(expectedFeedId, placement, density) ?: return null
        val state = states[stateKey(value.feedId, value.slotKey)] ?: return null
        if (value.generation < state.generation) return null
        val minimumWidth = (if (state.isSmall) 120 else 144) * density
        val minimumHeight = (if (state.isSmall) 120 else 300) * density
        if (value.rectWidth < minimumWidth.roundToInt() || value.rectHeight < minimumHeight.roundToInt()) return null

        if (!state.isSmall) {
            val adView = state.adView
            if (adView.isLayoutRequested || adView.measuredWidth != value.rectWidth || adView.measuredHeight != value.rectHeight) {
                adView.measure(
                    View.MeasureSpec.makeMeasureSpec(value.rectWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(value.rectHeight, View.MeasureSpec.EXACTLY),
                )
                adView.layout(0, 0, value.rectWidth, value.rectHeight)
            }
            val media = adView.mediaView ?: return null
            if (media.measuredWidth < 120 * density || media.measuredHeight < 120 * density) return null
        }

        val webView = webViewSupplier.get() ?: return null
        val wasVisible = state.clippingContainer.visibility == View.VISIBLE
        val clipParams = state.clippingContainer.layoutParams
        if (clipParams.width != value.clipWidth || clipParams.height != value.clipHeight) {
            state.clippingContainer.layoutParams = clipParams.apply {
                width = value.clipWidth
                height = value.clipHeight
            }
        }
        state.clippingContainer.x = webView.x + value.clipX
        state.clippingContainer.y = webView.y + value.clipY
        val adParams = state.adView.layoutParams
        if (adParams.width != value.rectWidth || adParams.height != value.rectHeight) {
            state.adView.layoutParams = FrameLayout.LayoutParams(value.rectWidth, value.rectHeight)
        }
        state.adView.x = (value.rectX - value.clipX).toFloat()
        state.adView.y = (value.rectY - value.clipY).toFloat()
        state.generation = value.generation
        state.clippingContainer.visibility = View.VISIBLE
        if (!wasVisible) state.clippingContainer.bringToFront()
        return stateKey(value.feedId, value.slotKey)
    }

    private fun overlayParent(): ViewGroup? = webViewSupplier.get()?.parent as? ViewGroup

    private fun destroyState(stateKey: String) {
        val state = states.remove(stateKey) ?: return
        (state.clippingContainer.parent as? ViewGroup)?.removeView(state.clippingContainer)
        state.adView.destroy()
        state.nativeAd.destroy()
    }

    private fun cancelPendingLoad(stateKey: String) {
        pendingLoads.remove(stateKey)?.call?.reject("Native ad load was cancelled")
    }

    private fun isCurrent(stateKey: String, loadToken: Any): Boolean = states[stateKey]?.loadToken === loadToken

    private fun isCurrentSession(feedId: String, sessionId: String): Boolean = feedSessions.isCurrent(feedId, sessionId)

    private fun clearFeed(feedId: String) {
        pendingLoads.filterValues { it.feedId == feedId }.keys.toList().forEach(::cancelPendingLoad)
        states.filterValues { it.feedId == feedId }.keys.toList().forEach(::destroyState)
    }

    private fun requiredString(call: PluginCall, name: String): String? {
        val value = call.getString(name)?.trim()
        if (value.isNullOrEmpty()) {
            call.reject("$name must not be empty")
            return null
        }
        return value
    }

    private fun stateKey(feedId: String, slotKey: String): String = "$feedId\u0000$slotKey"

    private fun identity(feedId: String, sessionId: String, slotKey: String): JSObject = JSObject().apply {
        put("feedId", feedId)
        put("sessionId", sessionId)
        put("slotKey", slotKey)
    }

    companion object {
        const val NATIVE_TESTER_ID = "ca-app-pub-3940256099942544/2247696110"
        private const val MAX_ADS_PER_FEED = 3
    }
}
