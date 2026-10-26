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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class NativeAdExecutorTest {
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
