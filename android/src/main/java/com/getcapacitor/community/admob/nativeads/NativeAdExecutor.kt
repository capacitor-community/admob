package com.getcapacitor.community.admob.nativeads

import android.app.Activity
import android.content.Context
import io.github.webview_native_ui.engine.NativeUIProjection
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
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
import java.io.ByteArrayOutputStream
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
        val isTesting: Boolean,
        val opaqueBackground: Boolean,
        var generation: Int = -1,
        var projection: NativeUIProjection? = null,
        var projectionIdentity: JSObject? = null,
        var projectionVisible: Boolean = true,
        var deferredPlacement: JSONObject? = null,

    ) {
        var placementSequence: Long = -1
    }

    private data class PendingLoad(
        val call: PluginCall,
        val feedId: String,
        val token: Any = Any(),
    )

    private val states = mutableMapOf<String, NativeAdState>()
    private val pendingLoads = mutableMapOf<String, PendingLoad>()
    private val feedSessions = NativeAdFeedSessions()
    private val snapshotCapturePending = AtomicBoolean(false)

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
            applyPlacements(call.data) { hidden ->
                if (hidden) call.resolve() else call.reject("Native ad projection did not finish hiding")
            }
        }
    }

    private fun applyPlacements(batch: JSONObject, complete: ((Boolean) -> Unit)? = null) {
        val feedId = batch.optString("feedId")
        val sessionId = batch.optString("sessionId")
        if (!isCurrentSession(feedId, sessionId) || !feedSessions.accepts(feedId, batch.optLong("sequence", -1L))) { complete?.invoke(true); return }
        val sequence = batch.optLong("sequence", -1L)
        val eligible = states.filter { it.value.feedId == feedId && it.value.placementSequence < sequence }.keys.toSet()
        val shown = mutableSetOf<String>()
        val retained = mutableSetOf<String>()
        val placements = batch.optJSONArray("placements") ?: JSArray()
        repeat(placements.length()) { index ->
            try {
                val placement = placements.getJSONObject(index)
                val key = stateKey(feedId, placement.optString("slotKey"))
                if (key in eligible) {
                    if (placement.optBoolean("retain", false)) {
                        val state = states[key]
                        if (state != null && placement.optString("feedId") == feedId &&
                            placement.optInt("generation", -1) == state.generation && placement.optBoolean("visible", false) &&
                            state.projectionVisible && state.clippingContainer.visibility == View.VISIBLE) {
                            shown.add(key)
                            retained.add(key)
                        }
                    } else applyPlacement(feedId, placement)?.let { shown.add(it) }
                }
            } catch (_: JSONException) {
                // Invalid entries fail closed below.
            }
        }
        val hiding = mutableListOf<NativeUIProjection>()
        // Retain carries no new geometry. A delayed retain must not supersede
        // the final position arriving independently through the Engine channel.
        (eligible - retained).forEach { states[it]?.placementSequence = sequence }
        states.filter { it.key in eligible && it.key !in shown }.values.forEach {
            it.deferredPlacement = null
            it.clippingContainer.visibility = View.GONE
            if (it.projectionVisible || complete != null) it.projection?.let(hiding::add)
            if (it.projectionVisible) {
                it.projectionVisible = false
                it.projectionIdentity?.let { event -> notifyListeners("nativeAdProjectionHidden", event) }
            }
        }
        if (hiding.isEmpty()) complete?.invoke(true)
        else {
            var remaining = hiding.size
            var succeeded = true
            hiding.forEach { projection -> projection.hide { result ->
                succeeded = succeeded && result
                if (--remaining == 0) complete?.invoke(succeeded)
            } }
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

        val backgroundColor = (adView.background as? GradientDrawable)?.color?.defaultColor
        val state = NativeAdState(
            feedId, template == "small", pendingLoad.token, nativeAd, adView, clippingContainer,
            pendingLoad.call.getBoolean("isTesting", false) == true,
            backgroundColor != null && Color.alpha(backgroundColor) == 255,
        )
        states[stateKey] = state
        overlayParent.addView(clippingContainer)
        pendingLoad.call.getString("projectionSelector")?.let { selector ->
            val event = identity(feedId, sessionId, slotKey).apply { put("loadId", java.util.UUID.randomUUID().toString()) }
            state.projectionIdentity = event
            val webView = webViewSupplier.get() as? WebView
            if (webView != null) webView.post {
                if (states[stateKey] !== state) return@post
                state.projection = runCatching {
                    NativeUIProjection(
                        window = activitySupplier.get().window,
                        webView = webView,
                        plugin = "AdMob",
                        projectionSelector = selector,
                        receiveListeners = NativeUIProjection.ReceiveListeners(
                            update = listOf("nativeAdProjectionUpdated"),
                            projection = NativeUIProjection.ReceiveListeners.Projection(
                                native = listOf("nativeAdProjectionShown"), none = listOf("nativeAdProjectionHidden")
                            ),
                            match = mapOf("feedId" to feedId, "sessionId" to sessionId, "slotKey" to slotKey, "loadId" to event.optString("loadId"))
                        ),
                        failedFallback = NativeUIProjection.FailedFallback.NATIVE,
                        captureMode = NativeUIProjection.CaptureMode.VIEW,
                        updatePlacement = { placement ->
                            check(states[stateKey] === state && isCurrentSession(feedId, sessionId)) { "Native ad owner changed" }
                            check(placement.optString("feedId") == feedId && placement.optString("sessionId") == sessionId &&
                                placement.optString("slotKey") == slotKey) { "Native ad placement identity changed" }
                            check(placement.optInt("generation", -1) >= state.generation &&
                                placement.optLong("sequence", -1L) > state.placementSequence && state.projectionVisible) {
                                "Native ad placement is stale or hidden"
                            }
                            if (!placement.optBoolean("visible", false)) {
                                state.placementSequence = placement.optLong("sequence")
                                state.deferredPlacement = null
                                state.clippingContainer.visibility = View.GONE
                                state.projectionVisible = false
                                state.projection?.hide()
                                error("Native ad placement is not visible")
                            }
                            check(applyPlacement(feedId, placement, defer = false) == stateKey) { "Native ad placement is invalid" }
                            state.placementSequence = placement.optLong("sequence")
                        },
                        beforeNativeReveal = {
                            val placement = state.deferredPlacement
                            state.deferredPlacement = null
                            if (states[stateKey] === state && placement != null) applyPlacement(feedId, placement, defer = false)
                        },
                        canCapture = { states[stateKey] === state && state.isTesting && state.opaqueBackground &&
                            nativeAd.mediaContent?.hasVideoContent() != true && isSnapshotSizeSupported(adView.width, adView.height) },
                        view = { if (states[stateKey] === state) adView else null }
                    )
                }.onFailure { android.util.Log.w("AdMob", "Native ad projection unavailable; retaining native tracking", it) }.getOrNull()
            }
        }
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

    private fun applyPlacement(expectedFeedId: String, placement: JSONObject, defer: Boolean = true): String? {
        val density = contextSupplier.get().resources.displayMetrics.density
        val value = NativeAdPlacementValue.parse(expectedFeedId, placement, density) ?: return null
        val state = states[stateKey(value.feedId, value.slotKey)] ?: return null
        if (value.generation < state.generation) return null
        val minimumWidth = (if (state.isSmall) 120 else 144) * density
        val minimumHeight = (if (state.isSmall) 120 else 300) * density
        if (value.rectWidth < minimumWidth.roundToInt() || value.rectHeight < minimumHeight.roundToInt()) return null

        if (defer && state.projection?.isProjecting == true &&
            state.adView.width == value.rectWidth && state.adView.height == value.rectHeight) {
            state.deferredPlacement = JSONObject(placement.toString())
            state.generation = value.generation
            return stateKey(value.feedId, value.slotKey)
        }
        state.deferredPlacement = null
        val resized = state.adView.width != value.rectWidth || state.adView.height != value.rectHeight

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
        if (!state.projectionVisible) {
            state.projection?.show()
            state.projectionVisible = true
            state.projectionIdentity?.let { notifyListeners("nativeAdProjectionShown", it) }
        }
        if (resized) state.projectionIdentity?.let { notifyListeners("nativeAdProjectionUpdated", it) }
        return stateKey(value.feedId, value.slotKey)
    }

    private fun overlayParent(): ViewGroup? = webViewSupplier.get()?.parent as? ViewGroup

    // Experimental visual proxy only. SDK views remain responsible for all ad interactions.
    fun captureSnapshot(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        val slotKey = requiredString(call, "slotKey") ?: return
        activitySupplier.get().runOnUiThread {
            val state = states[stateKey(feedId, slotKey)]
            if (!isCurrentSession(feedId, sessionId) || state == null || !state.isTesting) {
                call.reject("Snapshots require an active test ad")
                return@runOnUiThread
            }
            if (!state.opaqueBackground) {
                call.reject("Snapshot underlays require an opaque native ad background")
                return@runOnUiThread
            }
            if (state.nativeAd.mediaContent?.hasVideoContent() == true) {
                call.reject("Video ads retain native tracking in the snapshot experiment")
                return@runOnUiThread
            }
            val view = state.adView
            if (!view.isShown || view.isLayoutRequested || !isSnapshotSizeSupported(view.width, view.height)) {
                call.reject("Native ad is not ready for a bounded snapshot")
                return@runOnUiThread
            }
            // One bitmap/encoder per executor, including concurrent feeds.
            if (!snapshotCapturePending.compareAndSet(false, true)) {
                call.reject("Another native ad snapshot is being encoded")
                return@runOnUiThread
            }
            val started = SystemClock.elapsedRealtimeNanos()
            val drawCpuStart = Debug.threadCpuTimeNanos()
            val bitmap = try {
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            } catch (error: Exception) {
                snapshotCapturePending.set(false)
                call.reject("Could not allocate native ad snapshot", error)
                return@runOnUiThread
            }
            try {
                view.draw(Canvas(bitmap))
                val drawn = SystemClock.elapsedRealtimeNanos()
                val drawCpuMs = (Debug.threadCpuTimeNanos() - drawCpuStart) / 1_000_000.0
                // Compression must not block the UI thread when the next gesture begins.
                Thread {
                    try {
                        val encodeStart = SystemClock.elapsedRealtimeNanos()
                        val encodeCpuStart = Debug.threadCpuTimeNanos()
                        val bytes = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                        val dataUrl = "data:image/png;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP)
                        val timings = JSObject().apply {
                            put("drawMs", (drawn - started) / 1_000_000.0)
                            put("drawCpuMs", drawCpuMs)
                            put("workerWaitMs", (encodeStart - drawn) / 1_000_000.0)
                            put("encodeMs", (SystemClock.elapsedRealtimeNanos() - encodeStart) / 1_000_000.0)
                            put("encodeCpuMs", (Debug.threadCpuTimeNanos() - encodeCpuStart) / 1_000_000.0)
                            put("bitmapBytes", bitmap.byteCount)
                            put("pngBytes", bytes.size())
                        }
                        call.resolve(JSObject().put("dataUrl", dataUrl).put("timings", timings))
                    } catch (error: Exception) {
                        call.reject("Could not encode native ad snapshot", error)
                    } finally {
                        bitmap.recycle()
                        snapshotCapturePending.set(false)
                    }
                }.start()
            } catch (error: Exception) {
                bitmap.recycle()
                snapshotCapturePending.set(false)
                call.reject("Could not draw native ad snapshot", error)
            }
        }
    }

    fun prepareSnapshot(call: PluginCall) {
        val feedId = requiredString(call, "feedId") ?: return
        val sessionId = requiredString(call, "sessionId") ?: return
        val slotKey = requiredString(call, "slotKey") ?: return
        activitySupplier.get().runOnUiThread {
            val key = stateKey(feedId, slotKey)
            val state = states[key]
            val webView = webViewSupplier.get() as? WebView
            if (!isCurrentSession(feedId, sessionId) || state == null || !state.isTesting ||
                webView == null || !webView.isAttachedToWindow || !webView.isShown) {
                call.reject("Native ad snapshot WebView is no longer visible")
                return@runOnUiThread
            }
            val completed = AtomicBoolean(false)
            val handler = Handler(Looper.getMainLooper())
            val timeout = Runnable {
                if (completed.compareAndSet(false, true)) call.reject("Native ad snapshot visual state timed out")
            }
            handler.postDelayed(timeout, 2000)
            try {
                webView.postVisualStateCallback(0, object : WebView.VisualStateCallback() {
                    override fun onComplete(requestId: Long) {
                        if (!completed.compareAndSet(false, true)) return
                        handler.removeCallbacks(timeout)
                        if (!isCurrentSession(feedId, sessionId) || states[key] !== state ||
                            !webView.isAttachedToWindow || !webView.isShown) {
                            call.reject("Native ad snapshot was invalidated")
                            return
                        }
                        // Ready for the next WebView draw, not a claim that pixels have been presented.
                        webView.invalidate()
                        call.resolve()
                    }
                })
            } catch (error: Exception) {
                if (completed.compareAndSet(false, true)) {
                    handler.removeCallbacks(timeout)
                    call.reject("Could not prepare native ad snapshot", error)
                }
            }
        }
    }

    private fun destroyState(stateKey: String) {
        val state = states.remove(stateKey) ?: return
        state.projection?.close()
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
        internal fun isSnapshotSizeSupported(width: Int, height: Int): Boolean =
            width > 0 && height > 0 && width.toLong() * height <= 2_000_000
    }
}
