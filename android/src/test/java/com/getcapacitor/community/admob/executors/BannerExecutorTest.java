package com.getcapacitor.community.admob.executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.util.BiConsumer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BannerExecutorTest {
    final String LOG_TAG = "AdRewardHandlerTest Log Tag";

    @Mock(lenient = true)
    Context contextMock;

    @Mock
    Activity activityMock;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    @Mock
    ViewGroup viewGroupMock = mock(ViewGroup.class);

    MockedConstruction<AdView> adViewMockedConstruction;

    BannerExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(contextMock, activityMock, notifierMock);
        adViewMockedConstruction = Mockito.mockConstruction(AdView.class);

        when(activityMock.findViewById(anyInt())).thenReturn(viewGroupMock);
        when(viewGroupMock.getChildAt(anyInt())).thenReturn(viewGroupMock);

        sut = new BannerExecutor(() -> contextMock, () -> activityMock, notifierMock, LOG_TAG);
    }

    @AfterEach
    void afterEach() {
        adViewMockedConstruction.close();
    }

    @Test
    @DisplayName("#Initialize gets the reference of the reference of the viewGroup where the banner ad will go")
    void initialize() {
        sut.initialize();
        verify(viewGroupMock).getChildAt(0);
    }

    @Nested
    @DisplayName("Show Banner")
    class ShowBanner {
        @Mock
        MockedConstruction<RelativeLayout> relativeLayoutMockedConstruction;

        @Mock
        MockedConstruction<CoordinatorLayout.LayoutParams> layoutParamsMockedConstruction;

        @Mock
        MockedStatic<AdOptions> adOptionsMockedStatic;

        @Mock
        MockedStatic<RequestHelper> requestHelperMockedStatic;

        @Mock
        MockedStatic<AdViewIdHelper> adViewIdHelperMockedStatic;

        @Mock
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock
        Resources resourcesMock;

        @Mock
        DisplayMetrics displayMetricsMock;

        ArgumentCaptor<Runnable> runnableArgumentCaptor;
        AdOptions adOptionsMockForTesting;

        @BeforeEach
        void beforeEach() {
            reset(adOptionsFactoryMock, resourcesMock, displayMetricsMock);
            runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
            displayMetricsMock.density = 1f;

            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().build();

            adOptionsMockedStatic.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);

            when(contextMock.getResources()).thenReturn(resourcesMock);
            when(resourcesMock.getDisplayMetrics()).thenReturn(displayMetricsMock);

            sut.initialize();
        }

        @AfterEach
        void afterEach() {
            adOptionsMockedStatic.close();
            relativeLayoutMockedConstruction.close();
            layoutParamsMockedConstruction.close();
            requestHelperMockedStatic.close();
            adViewIdHelperMockedStatic.close();
        }

        @Test
        @DisplayName("Banner constructs the request using the RequestHelper")
        void showBannerUsesRequestHelper() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock);
            verify(activityMock).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            requestHelperMockedStatic.verify(() -> RequestHelper.createRequest(adOptionsMockForTesting));
        }

        @Test
        @DisplayName("Updates the banner if more than one show request is done")
        void showBanner() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock);
            sut.showBanner(pluginCallMock);

            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            List<Runnable> uiThreadRunnableSecondCall = runnableArgumentCaptor.getAllValues();
            uiThreadRunnableSecondCall.forEach(Runnable::run);

            AdView adViewMocked = adViewMockedConstruction.constructed().get(0);
            verify(adViewMocked, times(2)).loadAd(any());
        }
    }

    @Nested
    @DisplayName("Hide Banner")
    class HideBanner {
        @Mock
        MockedConstruction<RelativeLayout> relativeLayoutMockedConstruction;

        @Mock
        MockedConstruction<CoordinatorLayout.LayoutParams> layoutParamsMockedConstruction;

        @Mock(lenient = true)
        MockedStatic<AdOptions> adOptionsMockedStatic;

        @Mock(lenient = true)
        MockedStatic<RequestHelper> requestHelperMockedStatic;

        @Mock(lenient = true)
        MockedStatic<AdViewIdHelper> adViewIdHelperMockedStatic;

        @Mock(lenient = true)
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock(lenient = true)
        Resources resourcesMock;

        @Mock(lenient = true)
        DisplayMetrics displayMetricsMock;

        ArgumentCaptor<Runnable> runnableArgumentCaptor;
        AdOptions adOptionsMockForTesting;

        @BeforeEach
        void beforeEach() {
            reset(adOptionsFactoryMock, resourcesMock, displayMetricsMock);
            runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
            displayMetricsMock.density = 1f;

            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().build();

            adOptionsMockedStatic.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);

            when(contextMock.getResources()).thenReturn(resourcesMock);
            when(resourcesMock.getDisplayMetrics()).thenReturn(displayMetricsMock);

            sut.initialize();
        }

        @AfterEach
        void afterEach() {
            adOptionsMockedStatic.close();
            relativeLayoutMockedConstruction.close();
            layoutParamsMockedConstruction.close();
            requestHelperMockedStatic.close();
            adViewIdHelperMockedStatic.close();
        }

        @Test
        @DisplayName("Hides the banner if it exist")
        void hideBanner() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock);
            sut.hideBanner(pluginCallMock);

            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            List<Runnable> uiThreadRunnableSecondCall = runnableArgumentCaptor.getAllValues();
            uiThreadRunnableSecondCall.forEach(Runnable::run);

            AdView adViewMocked = adViewMockedConstruction.constructed().get(0);
            verify(adViewMocked, times(1)).pause();
        }

        @Test
        @DisplayName("If not banner exist, return an error")
        void hideBannerWithoutExistentBanner() {
            PluginCall pluginCallMock = mock(PluginCall.class);
            assertEquals(0, adViewMockedConstruction.constructed().size()); // Correct environment

            sut.hideBanner(pluginCallMock);

            verify(activityMock, times(0)).runOnUiThread(runnableArgumentCaptor.capture()); // No Ui Calls
            verify(pluginCallMock, times(1)).error(any());
        }
    }
}
