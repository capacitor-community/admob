package com.getcapacitor.community.admob.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.rewarded.AdRewardExecutor;
import com.getcapacitor.community.admob.rewarded.RewardAdPluginEvents;
import com.getcapacitor.community.admob.rewardedinterstitial.AdRewardInterstitialExecutor;
import com.getcapacitor.community.admob.rewardedinterstitial.RewardInterstitialAdPluginEvents;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.common.util.BiConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class RewardedAdShowCallbackTest {

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void settlesCancellationAndAllowsNextSession(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.show();
        h.callback.onAdDismissedFullScreenContent();
        h.callback.onAdDismissedFullScreenContent();
        h.reward.onUserEarnedReward(mock(RewardItem.class));
        verify(h.call).reject(anyString(), eq("REWARDED_AD_CANCELLED"));
        verify(h.call, never()).resolve(any());
        verify(h.events).accept(eq(h.dismissed), any());
        assertFalse(h.prepared());
        h.load();
        h.show();
        h.verifyShow(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void rewardOnlyResolvesOnceAndDismissalStillCleansUp(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.show();
        RewardItem item = mock(RewardItem.class);
        when(item.getType()).thenReturn("coin");
        when(item.getAmount()).thenReturn(7);
        h.reward.onUserEarnedReward(item);
        h.reward.onUserEarnedReward(item);
        h.callback.onAdDismissedFullScreenContent();
        ArgumentCaptor<JSObject> result = ArgumentCaptor.forClass(JSObject.class);
        verify(h.call).resolve(result.capture());
        assertEquals(7, result.getValue().getInteger("amount").intValue());
        verify(h.call, never()).reject(anyString(), anyString());
        verify(h.events).accept(eq(h.rewarded), any());
        verify(h.events).accept(eq(h.dismissed), any());
        assertFalse(h.prepared());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void presentationFailureRejectsWithSdkCode(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.show();
        AdError error = mock(AdError.class);
        when(error.getCode()).thenReturn(9);
        when(error.getMessage()).thenReturn("presentation failed");
        h.callback.onAdFailedToShowFullScreenContent(error);
        h.callback.onAdDismissedFullScreenContent();
        verify(h.call).reject("presentation failed", "9");
        verify(h.events).accept(eq(h.failed), any());
        verify(h.call, never()).resolve(any());
        assertFalse(h.prepared());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void synchronousUiThreadFailureDoesNotStrandCall(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.failShow();
        h.show();
        verify(h.call).reject("native failure", "REWARDED_AD_SHOW_FAILED");
        assertFalse(h.prepared());
        h.load();
        h.allowShow();
        h.show();
        h.verifyShow(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void concurrentShowCannotOverwriteActiveCall(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.show();
        PluginCall second = mock(PluginCall.class);
        h.invoke(second);
        verify(second).reject(anyString(), eq("REWARDED_AD_ALREADY_SHOWING"));
        h.verifyShow(1);
        h.callback.onAdDismissedFullScreenContent();
        verify(h.call).reject(anyString(), eq("REWARDED_AD_CANCELLED"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void oldDismissalDoesNotRemoveNewlyPreparedAd(boolean interstitial) {
        Harness h = new Harness(interstitial);
        h.show();
        if (interstitial) AdRewardInterstitialExecutor.preparedAds.put("test", mock(RewardedInterstitialAd.class));
        else AdRewardExecutor.preparedAds.put("test", mock(RewardedAd.class));
        h.callback.onAdDismissedFullScreenContent();
        assertTrue(h.prepared());
    }

    private static class Harness {

        final Activity activity = mock(Activity.class);
        final PluginCall call = mock(PluginCall.class);
        final BiConsumer<String, JSObject> events = mock(BiConsumer.class);
        // Use an interface to exercise the same contract against both SDK ad types.
        final RewardedAd ad = mock(RewardedAd.class);
        final RewardedInterstitialAd interstitialAd = mock(RewardedInterstitialAd.class);
        final boolean interstitial;
        final AdRewardExecutor executor;
        final AdRewardInterstitialExecutor interstitialExecutor;
        final String dismissed, rewarded, failed;
        FullScreenContentCallback callback;
        OnUserEarnedRewardListener reward;

        Harness(boolean interstitial) {
            this.interstitial = interstitial;
            AdRewardExecutor.preparedAds.clear();
            AdRewardInterstitialExecutor.preparedAds.clear();
            executor = new AdRewardExecutor(() -> mock(Context.class), () -> activity, events, "test");
            interstitialExecutor = new AdRewardInterstitialExecutor(() -> mock(Context.class), () -> activity, events, "test");
            dismissed = interstitial
                ? RewardInterstitialAdPluginEvents.INSTANCE.getDismissed()
                : RewardAdPluginEvents.INSTANCE.getDismissed();
            rewarded = interstitial ? RewardInterstitialAdPluginEvents.Rewarded : RewardAdPluginEvents.Rewarded;
            failed = interstitial
                ? RewardInterstitialAdPluginEvents.INSTANCE.getFailedToShow()
                : RewardAdPluginEvents.INSTANCE.getFailedToShow();
            doAnswer((invocation) -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            })
                .when(activity)
                .runOnUiThread(any());
            if (interstitial) {
                doAnswer((invocation) -> {
                    callback = invocation.getArgument(0);
                    return null;
                })
                    .when(interstitialAd)
                    .setFullScreenContentCallback(any());
                doAnswer((invocation) -> {
                    reward = invocation.getArgument(1);

                    return null;
                })
                    .when(interstitialAd)
                    .show(any(), any());
            } else {
                doAnswer((invocation) -> {
                    callback = invocation.getArgument(0);
                    return null;
                })
                    .when(ad)
                    .setFullScreenContentCallback(any());
                doAnswer((invocation) -> {
                    reward = invocation.getArgument(1);
                    return null;
                })
                    .when(ad)
                    .show(any(), any());
            }
            load();
        }

        void verifyShow(int count) {
            if (interstitial) verify(interstitialAd, times(count)).show(any(), any());
            else verify(ad, times(count)).show(any(), any());
        }

        void failShow() {
            if (interstitial) doThrow(new IllegalStateException("native failure")).when(interstitialAd).show(any(), any());
            else doThrow(new IllegalStateException("native failure")).when(ad).show(any(), any());
        }

        void allowShow() {
            if (interstitial) doNothing().when(interstitialAd).show(any(), any());
            else doNothing().when(ad).show(any(), any());
        }

        void load() {
            if (interstitial) {
                AdRewardInterstitialExecutor.preparedAds.put("test", interstitialAd);
                AdRewardInterstitialExecutor.lastPreparedAdId = "test";
            } else {
                AdRewardExecutor.preparedAds.put("test", ad);
                AdRewardExecutor.lastPreparedAdId = "test";
            }
        }

        void invoke(PluginCall next) {
            if (interstitial) interstitialExecutor.showRewardInterstitialAd(next, events);
            else executor.showRewardVideoAd(next, events);
        }

        void show() {
            invoke(call);
        }

        boolean prepared() {
            return interstitial
                ? AdRewardInterstitialExecutor.preparedAds.containsKey("test")
                : AdRewardExecutor.preparedAds.containsKey("test");
        }
    }
}
