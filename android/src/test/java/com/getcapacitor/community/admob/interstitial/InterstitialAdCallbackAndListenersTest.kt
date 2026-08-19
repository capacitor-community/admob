package com.getcapacitor.community.admob.interstitial

import android.app.Activity
import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import java.util.function.BiConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class InterstitialAdCallbackAndListenersTest {
    private val adUnitId = "test-ad-unit-id"



    @Mock
    lateinit var context: Context

    @Mock
    lateinit var  activity: Activity

    @Mock
    lateinit var  notifierMock: BiConsumer<String, JSObject>

    @Mock
    lateinit var pluginCall: PluginCall

    @BeforeEach
    fun beforeEach() {
        Mockito.reset(context, activity, notifierMock)
        Mockito.verify(pluginCall, never()).resolve(any()) // Always a clean call
    }

    @Nested
    inner class InterstitialAdLoadCallback {


        @Nested
        inner class OnAdFailedToLoad {
            private var wantedMessage = "This is the reason"
            private var wantedErrorCode: Int = 0

            @Mock
            lateinit var loadAdErrorMock: LoadAdError

            @BeforeEach
            fun beforeEach() {
                Mockito.`when`(loadAdErrorMock.code).thenReturn(LoadAdError.ErrorCode.values().first())
                Mockito.`when`(loadAdErrorMock.message).thenReturn(wantedMessage)
            }

            @Test
            fun `onAdFailedToLoad should emit the the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(InterstitialAdPluginPluginEvent.FailedToLoad), argumentCaptor.capture())
                val emittedError = argumentCaptor.value

                assertEquals(wantedErrorCode, emittedError.getInt("code"))
                assertEquals(wantedMessage, emittedError.getString("message"))
            }

            @Test
            fun `onAdFailedToLoad should reject the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(String::class.java)
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(pluginCall).reject(argumentCaptor.capture())
                val resolvedError = argumentCaptor.value
                assertEquals(wantedMessage, resolvedError)
            }

        }

        @Nested
        inner class AdLoaded {
            @Mock
            lateinit var interstitialAdMock: InterstitialAd

            @BeforeEach
            fun beforeEach() {
                Mockito.reset(interstitialAdMock)
            }

            @Test
            fun `onAdLoaded should emit an Loaded with the ad unit id`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)

                // ACt
                listener.onAdLoaded(interstitialAdMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(InterstitialAdPluginPluginEvent.Loaded), argumentCaptor.capture())
                val emittedAdInfo = argumentCaptor.value

                assertEquals(adUnitId, emittedAdInfo.getString("adUnitId"))
            }

            @Test
            fun `onAdLoaded should resolve  the ad unit id`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)

                // ACt
                listener.onAdLoaded(interstitialAdMock)

                Mockito.verify(pluginCall).resolve(argumentCaptor.capture())
                val resolvedInfo = argumentCaptor.value
                assertEquals(adUnitId, resolvedInfo.getString("adUnitId"))
            }

            @Test
            fun `onAdLoaded should store the ad on the static reference`() {
                AdInterstitialExecutor.preparedAds.clear()
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)

                // ACt
                listener.onAdLoaded(interstitialAdMock)
                Mockito.verify(pluginCall).resolve(argumentCaptor.capture())
                assertEquals(AdInterstitialExecutor.preparedAds[adUnitId], interstitialAdMock)
            }

            @Test
            fun `onAdLoaded should assign the content callback`() {
                val listener = InterstitialAdCallbackAndListeners.getInterstitialAdLoadCallback(pluginCall, notifierMock, adUnitId)
                // ACt
                listener.onAdLoaded(interstitialAdMock)

                Mockito.verify(interstitialAdMock).adEventCallback = any()
            }
        }


    }


}
