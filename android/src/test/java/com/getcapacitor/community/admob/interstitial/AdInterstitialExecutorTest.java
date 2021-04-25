package com.getcapacitor.community.admob.interstitial;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdInterstitialExecutorTest {

    @Mock()
    Context context;

    @Mock()
    Activity mockedActivity;

    @Mock()
    BiConsumer<String, JSObject> notifierMock;

    final String LOG_TAG = "AdInterstitialExecutorTest Log Tag";

    AdInterstitialExecutor sut;

    ArgumentCaptor<Runnable> runnableArgumentCaptor;

    @BeforeEach
    void beforeEach() {

        runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);


        sut = new AdInterstitialExecutor(() -> context, () -> mockedActivity, notifierMock, LOG_TAG);
    }

    @AfterEach()
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
        MockedStatic<InterstitialAdCallbackAndListeners> interstitialAdCallbackAndListenersMockedStatic;

        @Mock
        InterstitialAdLoadCallback interstitialAdLoadCallbackMock;

        @Mock()
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock()
        PluginCall pluginCallMock;

        @Mock()
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

        @AfterEach()
        void afterEach() {
            reset(adOptionsFactoryMock);
            requestHelperMockedStatic.close();
            adOptionsMockedStatic.close();
            interstitialAdMockedStatic.close();
            adViewIdHelperMockedStatic.close();
            interstitialAdCallbackAndListenersMockedStatic.close();
        }

        @Test
        @DisplayName("creates the options with the correct adOption factory")
        void createTheOptions() {

            sut.prepareInterstitial(pluginCallMock, notifierMock);

            verify(adOptionsFactoryMock).createInterstitialOptions(pluginCallMock);
        }

        @Test()
        @DisplayName("loads the ad with the id and request of the helper")
        void usesIdHelper() {
            final ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<AdRequest> adRequestCaptor = ArgumentCaptor.forClass(AdRequest.class);

            sut.prepareInterstitial(pluginCallMock, notifierMock);
            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            interstitialAdMockedStatic.verify(() -> InterstitialAd.load(any(),
                    idArgumentCaptor.capture(),
                    adRequestCaptor.capture(),
                    any()
            ));


            assertEquals(idFromViewHelper, idArgumentCaptor.getValue());
            assertEquals(adRequestFromHelper, adRequestCaptor.getValue());
        }

        @Test()
        @DisplayName("loads the ad with the InterstitialAdLoadCallback returned by the helper")
        void usesCallbackHelper() {
            final ArgumentCaptor<InterstitialAdLoadCallback> callbackArgumentCaptor = ArgumentCaptor.forClass(InterstitialAdLoadCallback.class);
            InterstitialAdCallbackAndListeners callbackSpy = spy(InterstitialAdCallbackAndListeners.INSTANCE);
            sut.prepareInterstitial(pluginCallMock, notifierMock);
            verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            interstitialAdMockedStatic.verify(() -> InterstitialAd.load(any(),
                    any(),
                    any(),
                    callbackArgumentCaptor.capture()
            ));

            verify(callbackSpy).getInterstitialAdLoadCallback(pluginCallMock, notifierMock);

// TODO: Inject callback creators on the constructor.
        }
    }

}
