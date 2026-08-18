package com.getcapacitor.community.admob.rewarded

import android.app.Activity
import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AdOptions
import com.getcapacitor.community.admob.rewarded.models.SsvInfo
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import java.util.function.BiConsumer
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class RewardedAdCallbackAndListenersTest {
    private val adUnitId = "test-rewarded-ad-unit"

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var activity: Activity

    @Mock
    lateinit var notifierMock: BiConsumer<String, JSObject>

    @Mock
    lateinit var pluginCall: PluginCall

    private lateinit var listener: com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback<RewardedAd>

    @BeforeEach
    fun beforeEach() {
        Mockito.reset(context, activity, notifierMock)
        Mockito.verify(pluginCall, never()).resolve(any()) // Always a clean call
        listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(
            pluginCall,
            notifierMock, AdOptions.TesterAdOptionsBuilder().build(), adUnitId
        )
    }

    @Nested
    inner class OnUserEarnedRewardListener {
        private val wantedType = "My Type"
        private val wantedAmount = 69
        private val rewardItem: RewardItem = object : RewardItem {
            override val type: String = wantedType
            override val amount: Int = wantedAmount
        }

        @Test
        fun `onRewarded should emit the Reward Item info`() {
            val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            val listener = RewardedAdCallbackAndListeners.getOnUserEarnedRewardListener(
                pluginCall,
                notifierMock
            )

            // ACt
            listener.onUserEarnedReward(rewardItem)

            Mockito.verify(notifierMock).accept(
                ArgumentMatchers.eq(RewardAdPluginEvents.Rewarded),
                argumentCaptor.capture()
            )
            val emittedItem = argumentCaptor.value
            assertEquals(emittedItem.getString("type"), wantedType)
            assertEquals(emittedItem.getInt("amount"), wantedAmount)
        }

        @Test
        fun `onRewarded should resolve the Reward Item info`() {
            val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            val listener = RewardedAdCallbackAndListeners.getOnUserEarnedRewardListener(
                pluginCall,
                notifierMock
            )

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
            private var wantedErrorCode: Int = 0

            @Mock
            lateinit var loadAdErrorMock: LoadAdError


            @BeforeEach
            fun beforeEach() {
                Mockito.`when`(loadAdErrorMock.code).thenReturn(LoadAdError.ErrorCode.values().first())
                Mockito.`when`(loadAdErrorMock.message).thenReturn(wantedReason)
            }

            @Test
            fun `onAdFailedToLoad should emit the the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(
                    pluginCall,
                    notifierMock, AdOptions.TesterAdOptionsBuilder().build(), adUnitId
                )

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(notifierMock).accept(
                    ArgumentMatchers.eq(RewardAdPluginEvents.FailedToLoad),
                    argumentCaptor.capture()
                )
                val emittedError = argumentCaptor.value

                assertEquals(wantedErrorCode, emittedError.getInt("code"))
                assertEquals(wantedReason, emittedError.getString("message"))
            }

            @Test
            fun `onAdFailedToLoad should reject the error code and reason in a FailedToLoad event`() {
                val argumentCaptor = ArgumentCaptor.forClass(String::class.java)

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
            }

            @Test
            fun `onAdLoaded should emit an Loaded with the ad unit id`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)

                // ACt
                listener.onAdLoaded(rewardedAdMock)

                Mockito.verify(notifierMock).accept(
                    ArgumentMatchers.eq(RewardAdPluginEvents.Loaded),
                    argumentCaptor.capture()
                )
                val emittedAdInfo = argumentCaptor.value

                assertEquals(adUnitId, emittedAdInfo.getString("adUnitId"))
            }

            @Test
            fun `register server side verification customData when ssv info exist and it has customData`() {

                val adOptions =
                    AdOptions.TesterAdOptionsBuilder().setSsvInfo(SsvInfo("customData", null))
                        .build()

                listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(
                    pluginCall,
                    notifierMock, adOptions, adUnitId
                )

                // Act
                listener.onAdLoaded(rewardedAdMock)

                verify(pluginCall).resolve(any())
            }

            @Test
            fun `register server side verification userId data when ssv info exist and has userId`() {

                val adOptions =
                    AdOptions.TesterAdOptionsBuilder().setSsvInfo(SsvInfo(null, "userId"))
                        .build()

                listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(
                    pluginCall,
                    notifierMock, adOptions, adUnitId
                )

                // Act
                listener.onAdLoaded(rewardedAdMock)

                verify(pluginCall).resolve(any())
            }
        }


    }

    // TODO: JUST CHECK CALL CREATION
    @Nested
    inner class FullScreenContentCallback {
        private lateinit var argumentCaptor: ArgumentCaptor<JSObject>
        private lateinit var listener: RewardedAdEventCallback

        @BeforeEach
        fun beforeEach() {
            argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
            listener = RewardedAdCallbackAndListeners.getRewardedAdEventCallback(notifierMock)
        }

        @Nested
        inner class AdShowedFullScreenContent {

            @Test
            fun `onAdShowedFullScreenContent call Showed event listener `() {

                // ACt
                listener.onAdShowedFullScreenContent()

                Mockito.verify(notifierMock).accept(
                    ArgumentMatchers.eq(RewardAdPluginEvents.Showed),
                    argumentCaptor.capture()
                )
            }

            @Test
            fun `onAdFailedToShowFullScreenContent call FailedToShow event listener `() {
                var wantedReason = "This is the reason"
                var wantedErrorCode = 0
                var adErrorMock = Mockito.mock(FullScreenContentError::class.java)
                Mockito.`when`(adErrorMock.code).thenReturn(FullScreenContentError.ErrorCode.values().first())
                Mockito.`when`(adErrorMock.message).thenReturn(wantedReason)

                // ACt
                listener.onAdFailedToShowFullScreenContent(adErrorMock)

                Mockito.verify(notifierMock).accept(
                    ArgumentMatchers.eq(RewardAdPluginEvents.FailedToShow),
                    argumentCaptor.capture()
                )
                val emittedError = argumentCaptor.value

                assertEquals(wantedErrorCode, emittedError.getInt("code"))
                assertEquals(wantedReason, emittedError.getString("message"))
            }

            @Test
            fun `onAdDismissedFullScreenContent call Dismissed event listener `() {

                // ACt
                listener.onAdDismissedFullScreenContent()

                Mockito.verify(notifierMock).accept(
                    ArgumentMatchers.eq(RewardAdPluginEvents.Dismissed),
                    argumentCaptor.capture()
                )
            }
        }
    }
}