package com.getcapacitor.community.admob.banner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.util.BiConsumer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BannerExecutorTest {

    final String LOG_TAG = "BannerExecutorTest Log Tag";

    @Mock(lenient = true)
    Context contextMock;

    @Mock(lenient = true)
    Activity activityMock;

    @Mock(lenient = true)
    Window windowMock;

    @Mock(lenient = true)
    WindowManager windowManagerMock;

    @Mock(lenient = true)
    Display displayMock;

    @Mock(lenient = true)
    WindowManager.LayoutParams attributesMock;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    @Mock
    ViewGroup viewGroupMock = mock(ViewGroup.class);

    MockedConstruction<AdView> adViewMockedConstruction;

    BannerExecutor sut;

    MockedStatic<AdSize> AdSizeStaticMock;

    @BeforeEach
    void beforeEach() {
        AdSizeStaticMock = Mockito.mockStatic(AdSize.class);
        reset(contextMock, activityMock, notifierMock, windowMock, windowManagerMock, displayMock, attributesMock);
        adViewMockedConstruction = Mockito.mockConstruction(AdView.class, (mock, context) -> {});

        when(activityMock.getWindowManager()).thenReturn(windowManagerMock);
        when(activityMock.getWindow()).thenReturn(windowMock);

        when(windowMock.getDecorView()).thenReturn(viewGroupMock);

        when(windowManagerMock.getDefaultDisplay()).thenReturn(displayMock);
        when(windowMock.getAttributes()).thenReturn(attributesMock);

        when(activityMock.findViewById(anyInt())).thenReturn(viewGroupMock);
        when(viewGroupMock.getChildAt(anyInt())).thenReturn(viewGroupMock);

        sut = new BannerExecutor(() -> contextMock, () -> activityMock, notifierMock, LOG_TAG);
    }

    @AfterEach
    void afterEach() {
        adViewMockedConstruction.close();
        AdSizeStaticMock.close();
    }

    @Test
    @DisplayName("#awaitViewGroup gets the reference of the viewGroup where the banner ad will go")
    void awaitViewGroup() {
        sut.awaitViewGroup((found) -> {});
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

            sut.awaitViewGroup((found) -> {});
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

            sut.showBanner(pluginCallMock, true);
            verify(activityMock).runOnUiThread(runnableArgumentCaptor.capture());
            Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
            uiThreadRunnable.run();

            requestHelperMockedStatic.verify(() -> RequestHelper.createRequest(adOptionsMockForTesting));
        }

        @Test
        @DisplayName("Updates the banner if more than one show request is done")
        void showBanner() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock, true);
            sut.showBanner(pluginCallMock, true);

            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            List<Runnable> uiThreadRunnableSecondCall = runnableArgumentCaptor.getAllValues();
            uiThreadRunnableSecondCall.forEach(Runnable::run);

            AdView adViewMocked = adViewMockedConstruction.constructed().get(0);
            verify(adViewMocked, times(2)).loadAd(any());
        }

        @ParameterizedTest
        @CsvSource({
            "BOTTOM_CENTER, false, true,  24,   0, 7, 31, 0",
            "BOTTOM_CENTER, false, true, 24, 0, 7, 7, 24",
            "TOP_CENTER, false, true, 24, 0, 7, 7, 32",
            "BOTTOM_CENTER, true,  true,  24, 300, 7, 7, 0",
            "BOTTOM_CENTER, true,  true,  24,   0, 7, 31, 0",
            "BOTTOM_CENTER, false, true,   0,   0, 7, 7, 0",
            "BOTTOM_CENTER, true,  true,   0, 300, 7, 7, 0",
            "BOTTOM_CENTER, false, false, 24,   0, 7, 31, 0",
            "BOTTOM_CENTER, true,  false, 24, 300, 7, 31, 0",
            "TOP_CENTER,    false, true,  24,   0, 39, 7, 0",
            "TOP_CENTER,    true,  true,  24, 300, 39, 7, 0"
        })
        void appliesOnlyRemainingSafeAreaInsets(
            String position,
            boolean keyboardVisible,
            boolean systemBarsHandlesInsets,
            int safeBottom,
            int imeBottom,
            int expectedTop,
            int expectedBottom,
            int parentPadding
        ) {
            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().setPosition(position).setMargin(7).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);
            sut.showBanner(mock(PluginCall.class), systemBarsHandlesInsets);

            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            ViewTreeObserver.OnGlobalLayoutListener listener = captureInsetsListener(banner);
            Mockito.lenient().when(viewGroupMock.getPaddingTop()).thenReturn(parentPadding);
            Mockito.lenient().when(viewGroupMock.getPaddingBottom()).thenReturn(parentPadding);
            verify(viewGroupMock, never()).setOnApplyWindowInsetsListener(any());
            WindowInsetsCompat compat = mock(WindowInsetsCompat.class);
            when(compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
                Insets.of(0, 32, 0, safeBottom)
            );
            when(compat.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(keyboardVisible);
            if (keyboardVisible) {
                when(compat.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, imeBottom));
            }
            try (MockedStatic<ViewCompat> conversion = Mockito.mockStatic(ViewCompat.class)) {
                conversion.when(() -> ViewCompat.getRootWindowInsets(banner)).thenReturn(compat);
                clearInvocations(params);
                listener.onGlobalLayout();
                verify(params).setMargins(0, expectedTop, 0, expectedBottom);
            }
        }

        @Test
        void restoresMarginsAfterKeyboardClosesWithoutAccumulatingInsets() {
            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().setPosition("BOTTOM_CENTER").setMargin(7).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);
            sut.showBanner(mock(PluginCall.class), true);
            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            ViewTreeObserver.OnGlobalLayoutListener listener = captureInsetsListener(banner);
            Mockito.doAnswer((invocation) -> {
                params.topMargin = invocation.getArgument(1);
                params.bottomMargin = invocation.getArgument(3);
                return null;
            })
                .when(params)
                .setMargins(anyInt(), anyInt(), anyInt(), anyInt());
            WindowInsetsCompat compat = mock(WindowInsetsCompat.class);
            when(compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
                Insets.of(0, 32, 0, 24)
            );
            when(compat.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(false, true, false, false);
            when(compat.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, 300));
            try (MockedStatic<ViewCompat> conversion = Mockito.mockStatic(ViewCompat.class)) {
                conversion.when(() -> ViewCompat.getRootWindowInsets(banner)).thenReturn(compat);
                clearInvocations(params);
                for (int i = 0; i < 4; i++) {
                    listener.onGlobalLayout();
                }
                var order = inOrder(params);
                order.verify(params).setMargins(0, 7, 0, 31);
                order.verify(params).setMargins(0, 7, 0, 7);
                order.verify(params).setMargins(0, 7, 0, 31);
                verify(params, times(3)).setMargins(anyInt(), anyInt(), anyInt(), anyInt());
            }
        }

        private ViewTreeObserver.OnGlobalLayoutListener captureInsetsListener(RelativeLayout banner) {
            ViewTreeObserver observer = mock(ViewTreeObserver.class);
            when(banner.getViewTreeObserver()).thenReturn(observer);
            when(banner.getParent()).thenReturn(viewGroupMock);
            when(banner.getRootView()).thenReturn(viewGroupMock);
            Mockito.lenient().when(viewGroupMock.getHeight()).thenReturn(1000);
            ArgumentCaptor<View.OnAttachStateChangeListener> attach = ArgumentCaptor.forClass(View.OnAttachStateChangeListener.class);
            verify(banner).addOnAttachStateChangeListener(attach.capture());
            attach.getValue().onViewAttachedToWindow(banner);
            ArgumentCaptor<ViewTreeObserver.OnGlobalLayoutListener> layout = ArgumentCaptor.forClass(
                ViewTreeObserver.OnGlobalLayoutListener.class
            );
            verify(observer).addOnGlobalLayoutListener(layout.capture());
            when(observer.isAlive()).thenReturn(true);
            attach.getValue().onViewDetachedFromWindow(banner);
            verify(observer).removeOnGlobalLayoutListener(layout.getValue());
            return layout.getValue();
        }

        @Test
        void requestsInsetsAfterAttachingBanner() {
            sut.showBanner(mock(PluginCall.class), true);
            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            verify(banner, never()).requestApplyInsets();
            verify(activityMock).runOnUiThread(runnableArgumentCaptor.capture());
            runnableArgumentCaptor.getValue().run();
            var order = inOrder(viewGroupMock, banner);
            order.verify(viewGroupMock).addView(banner);
            order.verify(banner).requestApplyInsets();
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

            sut.awaitViewGroup((found) -> {});
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

            sut.showBanner(pluginCallMock, true);
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
            verify(pluginCallMock, times(1)).reject(any());
        }
    }
}
