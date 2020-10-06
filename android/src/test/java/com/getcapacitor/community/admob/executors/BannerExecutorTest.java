package com.getcapacitor.community.admob.executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.util.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BannerExecutorTest {
    @Mock
    Context contextMock;

    @Mock
    Activity activityMock;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    MockedConstruction<AdView> adViewMockedConstruction;

    final String LOG_TAG = "AdRewardHandlerTest Log Tag";

    BannerExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(contextMock, activityMock, notifierMock);
        adViewMockedConstruction = Mockito.mockConstruction(AdView.class);
        sut = new BannerExecutor(() -> contextMock, () -> activityMock, notifierMock, LOG_TAG);
    }

    @AfterEach
    void afterEach() {
        adViewMockedConstruction.close();
    }

    @Nested
    @DisplayName("Show Banner")
    class ShowBanner {
        MockedConstruction<RelativeLayout> relativeLayoutMockedConstruction;
        MockedConstruction<CoordinatorLayout.LayoutParams> layoutParamsMockedConstruction;
        MockedStatic<AdOptions> adOptionsStaticMocked;

        @Mock
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        @Mock
        Resources resourcesMock;

        @Mock
        DisplayMetrics displayMetricsMock;

        @BeforeEach
        void beforeEach() {
            reset(adOptionsFactoryMock, resourcesMock, displayMetricsMock);
            displayMetricsMock.density = 1f;

            adOptionsStaticMocked = Mockito.mockStatic(AdOptions.class);
            relativeLayoutMockedConstruction = Mockito.mockConstruction(RelativeLayout.class);
            layoutParamsMockedConstruction = Mockito.mockConstruction(CoordinatorLayout.LayoutParams.class);

            adOptionsStaticMocked.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(new AdOptions.TesterAdOptionsBuilder().build());

            when(contextMock.getResources()).thenReturn(resourcesMock);
            when(resourcesMock.getDisplayMetrics()).thenReturn(displayMetricsMock);
        }

        @AfterEach
        void afterEach() {
            adOptionsStaticMocked.close();
            relativeLayoutMockedConstruction.close();
            layoutParamsMockedConstruction.close();
        }

        @Test
        void showBanner() {
            PluginCall pluginCall = mock(PluginCall.class);
            ViewGroup mViewGroup = mock(ViewGroup.class);

            sut.showBanner(pluginCall, mViewGroup);

            assertEquals(sut, sut);
        }
    }
}
