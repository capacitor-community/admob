package com.getcapacitor.community.admob.nativeads

import android.app.Activity
import android.content.Context
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
        val call = spy(PluginCall(handler, "AdMob", "2", "updateNativeAdPlacements", data))
        doReturn(placements).`when`(call).getArray(org.mockito.ArgumentMatchers.eq("placements"), any(JSArray::class.java))

        executor.updatePlacements(call)

        // Rejected sequences return before inspecting the placements.
        verify(placements).length()
    }
}
