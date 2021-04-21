package com.getcapacitor.community.admob.interstitial;

import android.app.Activity;
import android.content.Context;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.common.util.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdInterstitialExecutorTest {

    @Mock
    Context context;

    @Mock
    Activity activity;

    @Mock
    BiConsumer<String, JSObject> notifierMock;


    final String LOG_TAG = "AdInterstitialExecutorTest Log Tag";

    AdInterstitialExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(context, activity, notifierMock);
        sut = new AdInterstitialExecutor(() -> context, () -> activity, notifierMock, LOG_TAG);
    }

    @Nested
    class PrepareInterstitial {

        @Mock
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock
        PluginCall pluginCallMock;

        @Mock
        AdOptions adOptionsMock;

        @Test
        @DisplayName("creates the options with the correct adOption factory")
        void loadTheAd() {
            when(adOptionsFactoryMock.createInterstitialOptions(any())).thenReturn(adOptionsMock);

//            sut.prepareInterstitial(pluginCallMock, notifierMock);
//            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
//            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
//            uiThreadRunnable.run();
//
//            requestHelperMockedStatic.verify(() -> RequestHelper.createRequest(adOptionsMock));
        }
    }
/*
    @Nested
    class Listeners {

        @Test
        @DisplayName("onRewarded should emit the Reward Item info")
        void onRewarded() throws JSONException {
            ArgumentCaptor<JSObject> argumentCaptor = ArgumentCaptor.forClass(JSObject.class);
            PluginCall pluginCall = mock(PluginCall.class);

            OnUserEarnedRewardListener listener = AdRewardExecutor.getOnUserEarnedRewardListener(pluginCall, notifierMock);
            String type = "My Type";
            int amount = 69;
            RewardItem rewardItem = new RewardItem() {
                @Override
                public String getType() {
                    return type;
                }

                @Override
                public int getAmount() {
                    return amount;
                }
            };

            // ACt
            listener.onUserEarnedReward(rewardItem);

            verify(notifierMock).accept(eq(FullScreenAdEventName.adDidDismissFullScreenContent.name()), argumentCaptor.capture());
            final JSObject emittedItem = argumentCaptor.getValue();

            assertEquals(emittedItem.getString("type"), type);
            assertEquals(emittedItem.getInt("amount"), amount);
        }

        @Test
        @DisplayName("onRewardedVideoAdFailedToLoad should emit the error code and description")
        void onRewardedVideoAdFailedToLoad() throws JSONException {
            try (MockedStatic<RequestHelper> requestHelperMockedStatic = Mockito.mockStatic(RequestHelper.class)) {
                String reason = "This is the reason";
                requestHelperMockedStatic.when(() -> RequestHelper.getRequestErrorReason(anyInt())).thenReturn(reason);
                ArgumentCaptor<JSObject> argumentCaptor = ArgumentCaptor.forClass(JSObject.class);
                PluginCall pluginCall = mock(PluginCall.class);
                BiConsumer<String, JSObject> notifierMock = mock(BiConsumer.class);
                RewardedAdLoadCallback listener = AdRewardExecutor.getRewardedAdLoadCallback(pluginCall, notifierMock);
                int errorCode = 1;

                class AdErrorClass { }
                class ResponseInfoClass { }

//                listener.onAdFailedToLoad(new LoadAdError(errorCode, reason, reason, new AdError(errorCode, reason, reason), new ResponseInfo()));
//
//                
//                verify(notifierMock).accept(eq(FullScreenAdEventName.onAdFailedToLoad.name()), argumentCaptor.capture());
//                final JSObject emittedError = argumentCaptor.getValue();
//
//                assertEquals(emittedError.getInt("code"), errorCode);
//                assertEquals(emittedError.getString("reason"), reason);
            }
        }
    }
    */
}
