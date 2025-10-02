package com.getcapacitor.community.admob.rewarded;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.common.util.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdRewardExecutorTest {

    @Mock
    Context context;

    @Mock
    Activity mockedActivity;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    final String LOG_TAG = "AdRewardExecutorTest Log Tag";

    AdRewardExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(context, mockedActivity, notifierMock);
        sut = new AdRewardExecutor(() -> context, () -> mockedActivity, notifierMock, LOG_TAG);
    }

    @Nested
    class ShowRewardVideoAd {

        @Mock
        PluginCall pluginCallMock;

        ArgumentCaptor<Runnable> runnableArgumentCaptor;

        @BeforeEach
        void beforeEach() {
            runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
            AdRewardExecutor.mRewardedAd = null;
        }

        @Test
        @DisplayName("Should reject the call when no Reward was prepared")
        void rejectsWhenNoLoaded() {
            final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            Mockito.verify(pluginCallMock).reject(argumentCaptor.capture());
            String resolvedError = argumentCaptor.getValue();

            assertThat(resolvedError, containsString("not prepared"));
        }

        @Test
        @DisplayName("Should emit a Fail to show when no Reward was prepared")
        void emitsFailToShowWhenNoLoaded() {
            final ArgumentCaptor<JSObject> argumentCaptor = ArgumentCaptor.forClass(JSObject.class);

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            Mockito.verify(notifierMock).accept(ArgumentMatchers.eq(RewardAdPluginEvents.FailedToLoad), argumentCaptor.capture());

            JSObject emittedError = argumentCaptor.getValue();

            assertThat(emittedError.getString("message"), containsString("not prepared"));
        }

        @Test
        @DisplayName("Should not try to call show when no Reward was prepared")
        void shouldNotCallShowWhenNotPrepared() {
            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(mockedActivity, times(0)).runOnUiThread(any());
        }

        @Test
        @DisplayName("Should call show when Reward was prepared")
        void shouldCallShowWhenPrepared() {
            RewardedAd mockedInterstitialAd = mock(RewardedAd.class);
            AdRewardExecutor.mRewardedAd = mockedInterstitialAd;

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            Mockito.verify(pluginCallMock, times(0)).reject(any());
            verify(mockedInterstitialAd).show(any(), any());
        }
    }
}
