package com.getcapacitor.community.admob.rewarded;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd;
import java.util.function.BiConsumer;
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
            AdRewardExecutor.preparedAds.clear();
            AdRewardExecutor.lastPreparedAdId = null;
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
            RewardedAd mockedRewardedAd = mock(RewardedAd.class);
            AdRewardExecutor.preparedAds.put("test-ad-id", mockedRewardedAd);
            AdRewardExecutor.lastPreparedAdId = "test-ad-id";

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            Mockito.verify(pluginCallMock, times(0)).reject(any());
            verify(mockedRewardedAd).show(any(), any());
        }

        @Test
        @DisplayName("Should show a specific reward ad when adId is provided")
        void shouldShowSpecificAdWhenAdIdProvided() {
            RewardedAd adOne = mock(RewardedAd.class);
            RewardedAd adTwo = mock(RewardedAd.class);
            AdRewardExecutor.preparedAds.put("reward-1", adOne);
            AdRewardExecutor.preparedAds.put("reward-2", adTwo);
            AdRewardExecutor.lastPreparedAdId = "reward-2";

            when(pluginCallMock.getString("adId")).thenReturn("reward-1");

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            runnableArgumentCaptor.getValue().run();

            verify(adOne).show(any(), any());
            verify(adTwo, times(0)).show(any(), any());
        }

        @Test
        @DisplayName("Should show the last prepared reward ad when no adId is provided")
        void shouldShowLastPreparedWhenNoAdId() {
            RewardedAd adOne = mock(RewardedAd.class);
            RewardedAd adTwo = mock(RewardedAd.class);
            AdRewardExecutor.preparedAds.put("reward-1", adOne);
            AdRewardExecutor.preparedAds.put("reward-2", adTwo);
            AdRewardExecutor.lastPreparedAdId = "reward-2";

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            runnableArgumentCaptor.getValue().run();

            verify(adTwo).show(any(), any());
            verify(adOne, times(0)).show(any(), any());
        }

        @Test
        @DisplayName("Should reject when requesting a non-existent reward adId")
        void shouldRejectWhenAdIdNotFound() {
            RewardedAd adOne = mock(RewardedAd.class);
            AdRewardExecutor.preparedAds.put("reward-1", adOne);
            AdRewardExecutor.lastPreparedAdId = "reward-1";

            when(pluginCallMock.getString("adId")).thenReturn("non-existent");

            sut.showRewardVideoAd(pluginCallMock, notifierMock);

            verify(pluginCallMock).reject(any());
            verify(mockedActivity, times(0)).runOnUiThread(any());
        }
    }
}
