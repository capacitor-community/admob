package com.getcapacitor.community.admob.rewarded

import android.app.Activity
import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.common.util.BiConsumer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class RewardedAdCallbackAndListenersTest {

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
    inner class OnUserEarnedRewardListener {
        private val wantedType = "My Type"
        private val wantedAmount = 69
        private val rewardItem: RewardItem = object : RewardItem {
            override fun getType(): String {
                return wantedType
            }

            override fun getAmount(): Int {
                return wantedAmount
            }
        }

        @Test
        fun `onRewarded should emit the Reward Item info`() {
            val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            val listener = RewardedAdCallbackAndListeners.getOnUserEarnedRewardListener(pluginCall, notifierMock)

            // ACt
            listener.onUserEarnedReward(rewardItem)

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.Rewarded), argumentCaptor.capture())
            val emittedItem = argumentCaptor.value
            assertEquals(emittedItem.getString("type"), wantedType)
            assertEquals(emittedItem.getInt("amount"), wantedAmount)
        }

        @Test
        fun `onRewarded should resolve the Reward Item info`() {
            val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            val listener = RewardedAdCallbackAndListeners.getOnUserEarnedRewardListener(pluginCall, notifierMock)

            // ACt
            listener.onUserEarnedReward(rewardItem)

            Mockito.verify(pluginCall).resolve(argumentCaptor.capture())
            val resolvedItem = argumentCaptor.value
            assertEquals(resolvedItem.getString("type"), wantedType)
            assertEquals(resolvedItem.getInt("amount"), wantedAmount)
        }
    }

    @Nested
    inner class RewardedAdLoadCallback {


        @Nested
        inner class OnAdFailedToLoad {
            private var wantedReason = "This is the reason"
            private var wantedErrorCode: Int = 1

            @Mock
            lateinit var loadAdErrorMock: LoadAdError

            @BeforeEach
            fun beforeEach() {
                Mockito.`when`(loadAdErrorMock.code).thenReturn(wantedErrorCode)
                Mockito.`when`(loadAdErrorMock.message).thenReturn(wantedReason)
            }

            @Test
            fun `onAdFailedToLoad should emit the the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(pluginCall, notifierMock)

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.FailedToLoad), argumentCaptor.capture())
                val emittedError = argumentCaptor.value

                assertEquals(wantedErrorCode, emittedError.getInt("code"))
                assertEquals(wantedReason, emittedError.getString("reason"))
            }

            @Test
            fun `onAdFailedToLoad should reject the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(String::class.java)
                val listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(pluginCall, notifierMock)

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(pluginCall).reject(argumentCaptor.capture())
                val resolvedError = argumentCaptor.value
                assertEquals(wantedReason, resolvedError)
            }

        }

        @Nested
        inner class AdLoaded {
            private val wantedAdUnitId = "My Unit Id"

            @Mock
            lateinit var rewardedAdMock: RewardedAd

            @BeforeEach
            fun beforeEach() {
                Mockito.`when`(rewardedAdMock.adUnitId).thenReturn(wantedAdUnitId)
            }

            @Test
            fun `onAdLoaded should emit an Loaded with the ad unit id`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(pluginCall, notifierMock)

                // ACt
                listener.onAdLoaded(rewardedAdMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.Loaded), argumentCaptor.capture())
                val emittedAdInfo = argumentCaptor.value

                assertEquals(wantedAdUnitId, emittedAdInfo.getString("adUnitId"))
            }
        }


    }

    // TODO: JUST CHECK CALL CREATION
    @Nested
    inner class FullScreenContentCallback {
        private lateinit var argumentCaptor: ArgumentCaptor<JSObject>
        private lateinit var  listener: com.google.android.gms.ads.FullScreenContentCallback

        @BeforeEach
        fun beforeEach() {
            argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            listener = FullscreenPluginCallback(
                    RewardAdPluginEvents, notifierMock)
        }

        @Nested
        inner class AdShowedFullScreenContent {

            @Test
            fun `onAdShowedFullScreenContent call Showed event listener `() {

                // ACt
                listener.onAdShowedFullScreenContent()

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.Showed), argumentCaptor.capture())
            }

            @Test
            fun `onAdFailedToShowFullScreenContent call FailedToShow event listener `() {
                 var wantedReason = "This is the reason"
                 var wantedErrorCode = 1
                var adErrorMock = Mockito.mock(AdError::class.java);
                Mockito.`when`(adErrorMock.code).thenReturn(wantedErrorCode)
                Mockito.`when`(adErrorMock.message).thenReturn(wantedReason)

                // ACt
                listener.onAdFailedToShowFullScreenContent(adErrorMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.FailedToShow), argumentCaptor.capture())
                val emittedError = argumentCaptor.value

                assertEquals(wantedErrorCode, emittedError.getInt("code"))
                assertEquals(wantedReason, emittedError.getString("reason"))
            }

            @Test
            fun `onAdDismissedFullScreenContent call Dismissed event listener `() {

                // ACt
                listener.onAdDismissedFullScreenContent()

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.Dismissed), argumentCaptor.capture())
            }
        }
    }
}