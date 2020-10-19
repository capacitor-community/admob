package com.getcapacitor.community.admob.executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.RewardedAdEventName;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.util.BiConsumer;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdRewardExecutorTest {
    @Mock
    Context context;

    @Mock
    Activity activity;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    final String LOG_TAG = "AdRewardHandlerTest Log Tag";

    AdRewardExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(context, activity, notifierMock);
        sut = new AdRewardExecutor(() -> context, () -> activity, notifierMock, LOG_TAG);
    }

    @Nested
    class Listeners {

        @Test
        @DisplayName("onRewarded should emit the Reward Item info")
        void onRewarded() throws JSONException {
            ArgumentCaptor<JSObject> argumentCaptor = ArgumentCaptor.forClass(JSObject.class);
            PluginCall pluginCall = mock(PluginCall.class);

            RewardedVideoAdListener listener = AdRewardExecutor.getRewardedVideoAdListener(pluginCall, notifierMock);
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
            listener.onRewarded(rewardItem);

            verify(notifierMock).accept(eq(RewardedAdEventName.onRewarded.name()), argumentCaptor.capture());
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
                RewardedVideoAdListener listener = AdRewardExecutor.getRewardedVideoAdListener(pluginCall, notifierMock);
                int errorCode = 1;

                listener.onRewardedVideoAdFailedToLoad(errorCode);

                verify(notifierMock).accept(eq(RewardedAdEventName.onRewardedVideoAdFailedToLoad.name()), argumentCaptor.capture());
                final JSObject emittedError = argumentCaptor.getValue();

                assertEquals(emittedError.getInt("code"), errorCode);
                assertEquals(emittedError.getString("reason"), reason);
            }
        }
    }
}
