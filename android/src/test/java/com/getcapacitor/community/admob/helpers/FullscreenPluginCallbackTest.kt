package com.getcapacitor.community.admob.helpers

import com.getcapacitor.JSObject
import com.getcapacitor.community.admob.models.LoadPluginEventNames
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.common.util.BiConsumer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class FullscreenPluginCallbackTest {

    object LoadPluginObject: LoadPluginEventNames {
        override val Showed: String
            get() = "ShowedEvent"
        override val FailedToShow: String
            get() = "FailedToShowEvent"
        override val Dismissed: String
            get() = "DismissedEvent"
    }

    @Mock
    lateinit var  notifierMock: BiConsumer<String, JSObject>

    private lateinit var argumentCaptor: ArgumentCaptor<JSObject>
    private lateinit var  sut: FullScreenContentCallback

    @BeforeEach
    fun beforeEach() {
        argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
        sut = FullscreenPluginCallback(LoadPluginObject, notifierMock)
    }

    @Nested
    inner class AdShowedFullScreenContent {

        @Test
        fun `onAdShowedFullScreenContent call Showed event listener `() {

            // ACt
            sut.onAdShowedFullScreenContent()

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(LoadPluginObject.Showed), argumentCaptor.capture())
        }

        @Test
        fun `onAdFailedToShowFullScreenContent call FailedToShow event listener `() {
            val wantedReason = "This is the reason"
            val wantedErrorCode = 1
            val adErrorMock = Mockito.mock(AdError::class.java);
            Mockito.`when`(adErrorMock.code).thenReturn(wantedErrorCode)
            Mockito.`when`(adErrorMock.message).thenReturn(wantedReason)

            // ACt
            sut.onAdFailedToShowFullScreenContent(adErrorMock)

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(LoadPluginObject.FailedToShow), argumentCaptor.capture())
            val emittedError = argumentCaptor.value

            Assertions.assertEquals(wantedErrorCode, emittedError.getInt("code"))
            Assertions.assertEquals(wantedReason, emittedError.getString("message"))
        }

        @Test
        fun `onAdDismissedFullScreenContent call Dismissed event listener `() {

            // ACt
            sut.onAdDismissedFullScreenContent()

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(LoadPluginObject.Dismissed), argumentCaptor.capture())
        }
    }
}
