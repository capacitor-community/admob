package com.getcapacitor.community.admob.nativeads

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import android.view.View
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.MessageHandler
import com.getcapacitor.PluginCall
import org.junit.Test
import org.junit.Assert.assertEquals
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class NativeAdExecutorTest {
    @Test
    fun olderBatchProtectsEachSlotsHandoffSequenceIndependently() {
        val activity = mock(Activity::class.java)
        doAnswer { it.getArgument<Runnable>(0).run(); null }
            .`when`(activity).runOnUiThread(any(Runnable::class.java))
        val executor = NativeAdExecutor({ mock(Context::class.java) }, { activity },
            { mock(View::class.java) }, { _, _ -> }, "test")
        executor.startFeed(PluginCall(mock(MessageHandler::class.java), "AdMob", "1", "startNativeAdFeed",
            JSObject("""{"feedId":"feed","sessionId":"session"}""")))
        val first = mock(FrameLayout::class.java)
        val second = mock(FrameLayout::class.java)
        val stateClass = NativeAdExecutor::class.java.declaredClasses.single { it.simpleName == "NativeAdState" }
        val constructor = stateClass.declaredConstructors.single { it.parameterCount == 13 }.apply { isAccessible = true }
        val sequence = stateClass.getDeclaredField("placementSequence").apply { isAccessible = true }
        fun state(container: FrameLayout, handoffSequence: Long): Any = constructor
            .newInstance("feed", true, Any(), mock(NativeAd::class.java), mock(NativeAdView::class.java), container,
                true, true, 1, null, null, true, null).also { sequence.setLong(it, handoffSequence) }
        @Suppress("UNCHECKED_CAST")
        val states = NativeAdExecutor::class.java.getDeclaredField("states").apply { isAccessible = true }
            .get(executor) as MutableMap<String, Any>
        states["feed\u0000first"] = state(first, 10L)
        states["feed\u0000second"] = state(second, 12L)
        // An older full-feed batch must not hide either independently restored slot.
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":5,"placements":[]}""")
        verify(first, never()).visibility = View.GONE
        verify(second, never()).visibility = View.GONE
        // A batch between the two handoffs applies only to the older slot.
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":11,"placements":[]}""")
        verify(first).visibility = View.GONE
        verify(second, never()).visibility = View.GONE
        assertEquals(11L, sequence.getLong(states.getValue("feed\u0000first")))
        assertEquals(12L, sequence.getLong(states.getValue("feed\u0000second")))
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":13,"placements":[]}""")
        verify(first, times(2)).visibility = View.GONE
        verify(second).visibility = View.GONE
    }

    @Test
    fun mixedRetainDoesNotChangeImageGeometryOrSupersedeAnInFlightHandoff() {
        val context = mock(Context::class.java)
        val resources = mock(Resources::class.java)
        val metrics = mock(DisplayMetrics::class.java).apply { density = 1f }
        `when`(context.resources).thenReturn(resources)
        `when`(resources.displayMetrics).thenReturn(metrics)
        val activity = mock(Activity::class.java)
        doAnswer { it.getArgument<Runnable>(0).run(); null }
            .`when`(activity).runOnUiThread(any(Runnable::class.java))
        val executor = NativeAdExecutor({ context }, { activity }, { mock(View::class.java) }, { _, _ -> }, "test")
        executor.startFeed(PluginCall(mock(MessageHandler::class.java), "AdMob", "1", "startNativeAdFeed",
            JSObject("""{"feedId":"feed","sessionId":"session"}""")))
        val imageContainer = mock(FrameLayout::class.java)
        val nativeContainer = mock(FrameLayout::class.java)
        val imageView = mock(NativeAdView::class.java)
        val nativeView = mock(NativeAdView::class.java)
        `when`(imageContainer.visibility).thenReturn(View.VISIBLE)
        `when`(nativeContainer.visibility).thenReturn(View.VISIBLE)
        `when`(nativeContainer.layoutParams).thenReturn(mock(ViewGroup.LayoutParams::class.java).apply { width=200; height=200 })
        `when`(nativeView.layoutParams).thenReturn(mock(ViewGroup.LayoutParams::class.java).apply { width=200; height=200 })
        `when`(nativeView.width).thenReturn(200)
        `when`(nativeView.height).thenReturn(200)
        val stateClass = NativeAdExecutor::class.java.declaredClasses.single { it.simpleName == "NativeAdState" }
        val constructor = stateClass.declaredConstructors.single { it.parameterCount == 13 }.apply { isAccessible = true }
        val sequence = stateClass.getDeclaredField("placementSequence").apply { isAccessible = true }
        val deferred = stateClass.getDeclaredField("deferredPlacement").apply { isAccessible = true }
        val pending = org.json.JSONObject("""{"prior":"geometry"}""")
        val imageState = constructor.newInstance("feed", true, Any(), mock(NativeAd::class.java), imageView,
            imageContainer, true, true, 1, null, null, true, pending)
        val nativeState = constructor.newInstance("feed", true, Any(), mock(NativeAd::class.java), nativeView,
            nativeContainer, true, true, 1, null, null, true, null)
        sequence.setLong(imageState, 10L)
        @Suppress("UNCHECKED_CAST")
        val states = NativeAdExecutor::class.java.getDeclaredField("states").apply { isAccessible = true }
            .get(executor) as MutableMap<String, Any>
        states["feed\u0000image"] = imageState
        states["feed\u0000native"] = nativeState
        val retain = """{"feedId":"feed","slotKey":"image","generation":1,"visible":true,"retain":true}"""
        val placement = """{"feedId":"feed","slotKey":"native","generation":1,"visible":true,
            "rect":{"x":10,"y":40,"width":200,"height":200},
            "clipRect":{"x":10,"y":40,"width":200,"height":200}}"""
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":20,"placements":[$retain,$placement]}""")
        verify(nativeContainer).y = 40f
        verify(nativeContainer).visibility = View.VISIBLE
        verify(imageContainer, never()).visibility = anyInt()
        verify(imageContainer, never()).x = anyFloat()
        verify(imageContainer, never()).y = anyFloat()
        verifyNoInteractions(imageView)
        org.junit.Assert.assertSame(pending, deferred.get(imageState))
        assertEquals(10L, sequence.getLong(imageState))
        assertEquals(20L, sequence.getLong(nativeState))
        // The final image placement was measured at 19 but arrives after retain batch 20.
        // This is the same per-slot freshness predicate used by updatePlacement.
        org.junit.Assert.assertTrue(19L > sequence.getLong(imageState))
        sequence.setLong(imageState, 19L)
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":21,"placements":[$retain,$placement]}""")
        assertEquals(19L, sequence.getLong(imageState))
        // A real later hide remains authoritative; retain cannot resurrect the slot.
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":22,"placements":[$placement]}""")
        executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":23,"placements":[$retain,$placement]}""")
        verify(imageContainer, never()).visibility = View.VISIBLE
        verify(imageContainer, times(2)).visibility = View.GONE
        org.junit.Assert.assertNull(deferred.get(imageState))
    }

    @Test
    fun mediumAdResizeNotifiesProjectionOnceAfterLayout() {
        val context = mock(Context::class.java)
        val resources = mock(Resources::class.java)
        val metrics = mock(DisplayMetrics::class.java).apply { density = 1f }
        `when`(context.resources).thenReturn(resources)
        `when`(resources.displayMetrics).thenReturn(metrics)
        val activity = mock(Activity::class.java)
        doAnswer { it.getArgument<Runnable>(0).run(); null }
            .`when`(activity).runOnUiThread(any(Runnable::class.java))
        val notifications = mutableListOf<String>()
        val executor = NativeAdExecutor(
            { context }, { activity }, { mock(View::class.java) },
            { name, _ -> notifications.add(name) }, "test",
        )
        val adView = mock(NativeAdView::class.java)
        val media = mock(MediaView::class.java)
        val container = mock(FrameLayout::class.java)
        val params = mock(ViewGroup.LayoutParams::class.java).apply { width = 320; height = 400 }
        `when`(adView.layoutParams).thenReturn(params)
        `when`(container.layoutParams).thenReturn(params)
        `when`(container.visibility).thenReturn(View.VISIBLE)
        `when`(adView.mediaView).thenReturn(media)
        `when`(media.measuredWidth).thenReturn(200)
        `when`(media.measuredHeight).thenReturn(200)
        var width = 300
        var height = 350
        `when`(adView.width).thenAnswer { width }
        `when`(adView.height).thenAnswer { height }
        `when`(adView.measuredWidth).thenAnswer { width }
        `when`(adView.measuredHeight).thenAnswer { height }
        doAnswer { call -> width = call.getArgument(2); height = call.getArgument(3); null }
            .`when`(adView).layout(anyInt(), anyInt(), anyInt(), anyInt())

        executor.startFeed(PluginCall(mock(MessageHandler::class.java), "AdMob", "1", "startNativeAdFeed",
            JSObject("""{"feedId":"feed","sessionId":"session"}""")))
        // Seed a loaded SDK ad; keep placement, layout and notification handling real.
        val stateClass = NativeAdExecutor::class.java.declaredClasses.single { it.simpleName == "NativeAdState" }
        val state = stateClass.declaredConstructors.single { it.parameterCount == 13 }.apply { isAccessible = true }
            .newInstance("feed", false, Any(), mock(NativeAd::class.java), adView, container,
                true, true, -1, null, JSObject().put("slotKey", "slot"), true, null)
        @Suppress("UNCHECKED_CAST")
        val states = NativeAdExecutor::class.java.getDeclaredField("states").apply { isAccessible = true }
            .get(executor) as MutableMap<String, Any>
        states["feed\u0000slot"] = state
        mockStatic(View.MeasureSpec::class.java).use {
            for (sequence in 1..2) {
                executor.updateDirectPlacements("""{"feedId":"feed","sessionId":"session","sequence":$sequence,
                    "placements":[{"feedId":"feed","slotKey":"slot","generation":$sequence,"visible":true,
                    "rect":{"x":0,"y":$sequence,"width":320,"height":400},
                    "clipRect":{"x":0,"y":$sequence,"width":320,"height":400}}]}""")
            }
        }
        org.junit.Assert.assertEquals(listOf("nativeAdProjectionUpdated"), notifications)
        verify(adView).layout(0, 0, 320, 400)
    }

    @Test
    fun acceptsPlacementSequenceParsedAsInteger() {
        val activity = mock(Activity::class.java)
        doAnswer { invocation -> invocation.getArgument<Runnable>(0).run(); null }
            .`when`(activity).runOnUiThread(any(Runnable::class.java))
        val executor = NativeAdExecutor(
            { mock(Context::class.java) }, { activity }, { mock(View::class.java) }, { _, _ -> }, "test",
        )
        val handler = mock(MessageHandler::class.java)
        val data = JSObject("""{"feedId":"feed","sessionId":"session","sequence":1}""")
        executor.startFeed(PluginCall(handler, "AdMob", "1", "startNativeAdFeed", data))
        val placements = mock(JSArray::class.java)
        data.put("placements", placements)
        val call = PluginCall(handler, "AdMob", "2", "updateNativeAdPlacements", data)

        executor.updatePlacements(call)

        // Rejected sequences return before inspecting the placements.
        verify(placements).length()
    }
}
