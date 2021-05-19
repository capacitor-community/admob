package com.getcapacitor.community.admob.interstitial;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.common.util.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdInterstitialExecutorTest {

    @Mock
    Context context;

    @Mock
    Activity mockedActivity;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    @Mock
    InterstitialAdCallbackAndListeners interstitialAdCallbackAndListenersMock;

    final String LOG_TAG = "AdInterstitialExecutorTest Log Tag";

    AdInterstitialExecutor sut;

    ArgumentCaptor<Runnable> runnableArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);

        sut =
            new AdInterstitialExecutor(() -> context, () -> mockedActivity, notifierMock, LOG_TAG, interstitialAdCallbackAndListenersMock);
    }

    @AfterEach
    void afterEach() {
        reset(context, mockedActivity, notifierMock);
    }

    @Nested
    class PrepareInterstitial {

        @Mock
        MockedStatic<AdOptions> adOptionsMockedStatic;

        @Mock
        MockedStatic<RequestHelper> requestHelperMockedStatic;

        @Mock
        MockedStatic<AdViewIdHelper> adViewIdHelperMockedStatic;

        @Mock
        MockedStatic<InterstitialAd> interstitialAdMockedStatic;

        @Mock
        InterstitialAdLoadCallback interstitialAdLoadCallbackMock;

        @Mock
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock
        PluginCall pluginCallMock;

        @Mock
        AdOptions adOptionsMock;

        AdRequest adRequestFromHelper = (new AdRequest.Builder()).build();

        final String idFromViewHelper = "The Id From The View Helper";

        @BeforeEach
        void beforeEach() {
            adOptionsMockedStatic.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);
            when(adOptionsFactoryMock.createInterstitialOptions(pluginCallMock)).thenReturn(adOptionsMock);
            requestHelperMockedStatic.when(() -> RequestHelper.createRequest(adOptionsMock)).thenReturn(adRequestFromHelper);
            adViewIdHelperMockedStatic.when(() -> AdViewIdHelper.getFinalAdId(any(), any(), any(), any())).thenReturn(idFromViewHelper);
        }

        @AfterEach
        void afterEach() {
            reset(adOptionsFactoryMock);
            requestHelperMockedStatic.close();
            adOptionsMockedStatic.close();
            interstitialAdMockedStatic.close();
            adViewIdHelperMockedStatic.close();
        }

        @Test
        @DisplayName("creates the options with the correct adOption factory")
        void createTheOptions() {
            sut.prepareInterstitial(pluginCallMock, notifierMock);

            verify(adOptionsFactoryMock).createInterstitialOptions(pluginCallMock);
        }

        @Test
        @DisplayName("loads the ad with the id and request of the helper")
        void usesIdHelper() {
            final ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<AdRequest> adRequestCaptor = ArgumentCaptor.forClass(AdRequest.class);

            sut.prepareInterstitial(pluginCallMock, notifierMock);
            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            interstitialAdMockedStatic.verify(
                () -> InterstitialAd.load(any(), idArgumentCaptor.capture(), adRequestCaptor.capture(), any())
            );

            assertEquals(idFromViewHelper, idArgumentCaptor.getValue());
            assertEquals(adRequestFromHelper, adRequestCaptor.getValue());
        }

        @Test
        @DisplayName("loads the ad with the InterstitialAdLoadCallback returned by the getInterstitialAdLoadCallback singleton")
        void usesCallbackHelper() {
            when(interstitialAdCallbackAndListenersMock.getInterstitialAdLoadCallback(pluginCallMock, notifierMock))
                .thenReturn(interstitialAdLoadCallbackMock);
            final ArgumentCaptor<InterstitialAdLoadCallback> callbackArgumentCaptor = ArgumentCaptor.forClass(
                InterstitialAdLoadCallback.class
            );

            sut.prepareInterstitial(pluginCallMock, notifierMock);
            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            interstitialAdMockedStatic.verify(() -> InterstitialAd.load(any(), any(), any(), callbackArgumentCaptor.capture()));

            final InterstitialAdLoadCallback callback = callbackArgumentCaptor.getValue();

            assertEquals(interstitialAdLoadCallbackMock, callback);
        }
    }

    @Nested
    class ShowInterstitial {

        @Mock
        PluginCall pluginCallMock;

        @AfterEach
        void afterEach() {
            AdInterstitialExecutor.interstitialAd = null;
        }

        @Test
        @DisplayName("Should reject the call when no Interstitial was prepared")
        void rejectsWhenNoInterstitialWasLoaded() {
            final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

            sut.showInterstitial(pluginCallMock, notifierMock);

            Mockito.verify(pluginCallMock).reject(argumentCaptor.capture());
            String resolvedError = argumentCaptor.getValue();

            assertThat(resolvedError, containsString("not prepared"));
        }

        @Test
        @DisplayName("Should emit a Fail to show when no Interstitial was prepared")
        void emitsFailToShowWhenNoInterstitialWasLoaded() {
            final ArgumentCaptor<JSObject> argumentCaptor = ArgumentCaptor.forClass(JSObject.class);

            sut.showInterstitial(pluginCallMock, notifierMock);

            Mockito
                .verify(notifierMock)
                .accept(ArgumentMatchers.eq(InterstitialAdPluginPluginEvent.FailedToLoad), argumentCaptor.capture());

            JSObject emittedError = argumentCaptor.getValue();

            assertThat(emittedError.getString("message"), containsString("not prepared"));
        }

        @Test
        @DisplayName("Should not try to call show when no Interstitial was prepared")
        void shouldNotCallShowWhenNotPrepared() {
            sut.showInterstitial(pluginCallMock, notifierMock);

            verify(mockedActivity, times(0)).runOnUiThread(any());
        }

        @Test
        @DisplayName("Should call show when Interstitial was prepared")
        void shouldCallShowWhenPrepared() {
            InterstitialAd mockedInterstitialAd = mock(InterstitialAd.class);
            AdInterstitialExecutor.interstitialAd = mockedInterstitialAd;

            sut.showInterstitial(pluginCallMock, notifierMock);

            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            Mockito.verify(pluginCallMock, times(0)).reject(any());
            verify(mockedInterstitialAd).show(any());
        }
    }
}
