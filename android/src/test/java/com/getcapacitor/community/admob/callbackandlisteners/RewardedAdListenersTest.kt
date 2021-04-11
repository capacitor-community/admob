package com.getcapacitor.community.admob.callbackandlisteners

import android.app.Activity
import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.FullScreenAdEventName
import com.getcapacitor.community.admob.models.RewardAdPluginEvents
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
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
internal class RewardedAdListenersTest {

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
    inner class GetOnUserEarnedRewardListener {
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

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.Rewarded.webEventName), argumentCaptor.capture())
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
            fun `onAdFailedToLoad should emit the the error code and reason`() {
                val argumentCaptor = ArgumentCaptor.forClass(JSObject::class.java)
                val listener = RewardedAdCallbackAndListeners.getRewardedAdLoadCallback(pluginCall, notifierMock)

                // ACt
                listener.onAdFailedToLoad(loadAdErrorMock)

                Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.FailedToLoad.webEventName), argumentCaptor.capture())
                val emittedError = argumentCaptor.value

                assertEquals(emittedError.getInt("code"), wantedErrorCode)
                assertEquals(emittedError.getString("reason"), wantedReason)
            }
        }


    }
}