package com.getcapacitor.community.admob.banner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
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

    private void runImmediatelyOnUiThread() {
        Mockito.doAnswer((invocation) -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        })
            .when(activityMock)
            .runOnUiThread(any());
    }

    @Nested
    @DisplayName("Show Banner")
    class ShowBanner {

        @Mock
        MockedStatic<android.util.Log> logMock;

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
            runImmediatelyOnUiThread();

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

        private void useExistingLayoutParams() {
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            when(relativeLayoutMockedConstruction.constructed().get(0).getLayoutParams()).thenReturn(params);
            clearInvocations(params);
        }

        private com.google.android.gms.ads.AdListener prepare(PluginCall call) {
            sut.prepareBanner(call, true);
            AdView ad = adViewMockedConstruction.constructed().get(0);
            AdSize size = mock(AdSize.class);
            Mockito.lenient().when(size.getWidth()).thenReturn(320);
            Mockito.lenient().when(size.getHeight()).thenReturn(50);
            Mockito.lenient().when(ad.getAdSize()).thenReturn(size);
            ArgumentCaptor<com.google.android.gms.ads.AdListener> listener = ArgumentCaptor.forClass(
                com.google.android.gms.ads.AdListener.class
            );
            verify(ad).setAdListener(listener.capture());
            return listener.getValue();
        }

        @Test
        void queuedRemovalCancelsPreparationBeforeItsViewExists() {
            PluginCall prepare = mock(PluginCall.class);
            PluginCall remove = mock(PluginCall.class);
            Mockito.doNothing().when(activityMock).runOnUiThread(any());
            sut.prepareBanner(prepare, true);
            sut.removeBanner(remove);
            verify(activityMock, times(2)).runOnUiThread(runnableArgumentCaptor.capture());
            List<Runnable> tasks = runnableArgumentCaptor.getAllValues();
            runImmediatelyOnUiThread();
            tasks.forEach(Runnable::run);
            verify(prepare).reject("Banner preparation was cancelled by removeBanner()");
            verify(remove).resolve();
            verify(prepare, never()).resolve();
        }

        @ParameterizedTest
        @CsvSource({ "resume", "show" })
        void queuedDisplayCannotOvertakePreparation(String action) {
            PluginCall prepare = mock(PluginCall.class);
            PluginCall display = mock(PluginCall.class);
            Mockito.doNothing().when(activityMock).runOnUiThread(any());
            sut.prepareBanner(prepare, true);
            if ("resume".equals(action)) sut.resumeBanner(display);
            else sut.showBanner(display, true);
            verify(activityMock, times(2)).runOnUiThread(runnableArgumentCaptor.capture());
            List<Runnable> tasks = runnableArgumentCaptor.getAllValues();
            runImmediatelyOnUiThread();
            tasks.forEach(Runnable::run);
            verify(display).reject(
                "resume".equals(action)
                    ? "Wait for prepareBanner() to finish before resuming the banner"
                    : "A banner is still being prepared"
            );
            verify(display, never()).resolve();
            assertEquals(1, adViewMockedConstruction.constructed().size());
            verify(relativeLayoutMockedConstruction.constructed().get(0), never()).setVisibility(View.VISIBLE);
            verify(prepare, never()).resolve();
            sut.removeBanner(mock(PluginCall.class));
        }

        @Test
        void preparesHiddenAndResumesWithoutAnotherAdRequest() {
            PluginCall call = mock(PluginCall.class);
            com.google.android.gms.ads.AdListener listener = prepare(call);
            RelativeLayout layout = relativeLayoutMockedConstruction.constructed().get(0);
            verify(layout).setVisibility(View.GONE);
            when(layout.getVisibility()).thenReturn(View.GONE);
            verify(call, never()).resolve();
            listener.onAdLoaded();
            verify(call).resolve();
            verify(notifierMock, never()).accept(Mockito.eq(BannerAdPluginEvents.SizeChanged.getWebEventName()), any());
            verify(notifierMock).accept(Mockito.eq(BannerAdPluginEvents.Loaded.getWebEventName()), any());
            PluginCall resume = mock(PluginCall.class);
            sut.resumeBanner(resume);
            verify(layout).setVisibility(View.VISIBLE);
            verify(resume).resolve();
            verify(notifierMock).accept(BannerAdPluginEvents.SizeChanged.getWebEventName(), new BannerAdSizeInfo(320, 50));
            verify(adViewMockedConstruction.constructed().get(0)).loadAd(any());
        }

        @Test
        void removalCancelsPreparationAndIgnoresLateLoad() {
            PluginCall call = mock(PluginCall.class);
            com.google.android.gms.ads.AdListener listener = prepare(call);
            PluginCall resume = mock(PluginCall.class);
            sut.resumeBanner(resume);
            verify(resume).reject("Wait for prepareBanner() to finish before resuming the banner");
            PluginCall second = mock(PluginCall.class);
            sut.prepareBanner(second, true);
            verify(second).reject("Remove the current banner before preparing another");
            sut.removeBanner(mock(PluginCall.class));
            verify(call).reject("Banner preparation was cancelled by removeBanner()");
            listener.onAdLoaded();
            verify(call, never()).resolve();
            verify(notifierMock, never()).accept(Mockito.eq(BannerAdPluginEvents.Loaded.getWebEventName()), any());
        }

        @Test
        void loadFailureRejectsPreparationAndAllowsRetry() {
            PluginCall call = mock(PluginCall.class);
            com.google.android.gms.ads.AdListener listener = prepare(call);
            com.google.android.gms.ads.LoadAdError error = mock(com.google.android.gms.ads.LoadAdError.class);
            when(error.getMessage()).thenReturn("No fill");
            when(error.getCode()).thenReturn(3);
            listener.onAdFailedToLoad(error);
            verify(call).reject("No fill", "3");
            verify(call, never()).resolve();
            sut.prepareBanner(mock(PluginCall.class), true);
            assertEquals(2, adViewMockedConstruction.constructed().size());
        }

        @Test
        @DisplayName("Banner constructs the request using the RequestHelper")
        void showBannerUsesRequestHelper() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock, true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());

            requestHelperMockedStatic.verify(() -> RequestHelper.createRequest(adOptionsMockForTesting));
        }

        @Test
        @DisplayName("Updates the banner if more than one show request is done")
        void showBanner() {
            PluginCall pluginCallMock = mock(PluginCall.class);

            sut.showBanner(pluginCallMock, true);
            useExistingLayoutParams();
            sut.showBanner(pluginCallMock, true);

            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());

            AdView adViewMocked = adViewMockedConstruction.constructed().get(0);
            verify(adViewMocked, times(2)).loadAd(any());
        }

        @ParameterizedTest
        @CsvSource({ "BOTTOM_CENTER, 75, 0, 75", "TOP_CENTER, 120, 120, 0", "CENTER, 200, 0, 0" })
        void updatesExistingBannerLayoutAndSettlesCall(String position, int margin, int top, int bottom) {
            PluginCall initial = mock(PluginCall.class);
            PluginCall update = mock(PluginCall.class);
            sut.showBanner(initial, true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            clearInvocations(activityMock);
            useExistingLayoutParams();

            AdOptions options = new AdOptions.TesterAdOptionsBuilder().setPosition(position).setMargin(margin).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(options);
            sut.showBanner(update, true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());

            assertEquals(1, adViewMockedConstruction.constructed().size());
            assertEquals(1, relativeLayoutMockedConstruction.constructed().size());
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            verify(params).setMargins(0, top, 0, bottom);
            assertEquals(
                "TOP_CENTER".equals(position)
                    ? android.view.Gravity.TOP
                    : "CENTER".equals(position)
                      ? android.view.Gravity.CENTER_VERTICAL
                      : android.view.Gravity.BOTTOM,
                params.gravity & android.view.Gravity.VERTICAL_GRAVITY_MASK
            );
            verify(relativeLayoutMockedConstruction.constructed().get(0), times(2)).requestApplyInsets();
            verify(adViewMockedConstruction.constructed().get(0)).loadAd(any());
            verify(update).resolve();

            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            ArgumentCaptor<View.OnApplyWindowInsetsListener> listener = ArgumentCaptor.forClass(View.OnApplyWindowInsetsListener.class);
            verify(banner).setOnApplyWindowInsetsListener(listener.capture());
            WindowInsets platformInsets = mock(WindowInsets.class);
            WindowInsetsCompat compat = mock(WindowInsetsCompat.class);
            when(compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
                Insets.of(0, 32, 0, 24)
            );
            try (MockedStatic<WindowInsetsCompat> conversion = Mockito.mockStatic(WindowInsetsCompat.class)) {
                conversion.when(() -> WindowInsetsCompat.toWindowInsetsCompat(platformInsets, banner)).thenReturn(compat);
                clearInvocations(params);
                listener.getValue().onApplyWindowInsets(banner, platformInsets);
                listener.getValue().onApplyWindowInsets(banner, platformInsets);
                verify(params, times(2)).setMargins(
                    0,
                    top + ("TOP_CENTER".equals(position) ? 32 : 0),
                    0,
                    bottom + ("TOP_CENTER".equals(position) ? 0 : 24)
                );
            }
        }

        @ParameterizedTest
        @CsvSource({ "ADAPTIVE_BANNER", "BANNER" })
        void centersUsingTheDisplayedSizeWhenUpdateSizeIsOmittedOrDifferent(BannerAdSizeEnum updateSize) {
            displayMetricsMock.widthPixels = 360;
            AdOptions initial = new AdOptions.TesterAdOptionsBuilder()
                .setAdSize(BannerAdSizeEnum.MEDIUM_RECTANGLE)
                .setPosition("BOTTOM_CENTER")
                .setMargin(0)
                .build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(initial);
            sut.showBanner(mock(PluginCall.class), true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            clearInvocations(activityMock);
            useExistingLayoutParams();
            AdView ad = adViewMockedConstruction.constructed().get(0);
            AdOptions update = new AdOptions.TesterAdOptionsBuilder()
                .setAdSize(updateSize)
                .setPosition("BOTTOM_CENTER")
                .setMargin(75)
                .build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(update);
            PluginCall call = mock(PluginCall.class);
            sut.showBanner(call, true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            verify(params).setMargins(0, 0, 0, 75);
            assertEquals(android.view.Gravity.CENTER_HORIZONTAL, params.gravity & android.view.Gravity.HORIZONTAL_GRAVITY_MASK);
            verify(ad).setAdSize(AdSize.MEDIUM_RECTANGLE);
            verify(ad).loadAd(any());
            verify(call).resolve();
        }

        @Test
        void reloadsWhenRequestOptionsChangeEvenAlongsideLayout() {
            sut.showBanner(mock(PluginCall.class), true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            clearInvocations(activityMock);
            useExistingLayoutParams();
            AdOptions update = new AdOptions.TesterAdOptionsBuilder().setNpa(true).setMargin(75).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(update);
            PluginCall call = mock(PluginCall.class);
            sut.showBanner(call, true);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
            requestHelperMockedStatic.verify(() -> RequestHelper.createRequest(update));
            verify(adViewMockedConstruction.constructed().get(0), times(2)).loadAd(any());
            verify(call).resolve();
        }

        @ParameterizedTest
        @CsvSource({
            "BOTTOM_CENTER, false, true,  24,   0, 0, 31",
            "BOTTOM_CENTER, true,  true,  24, 300, 0, 7",
            "BOTTOM_CENTER, true,  true,  24,   0, 0, 31",
            "BOTTOM_CENTER, false, true,   0,   0, 0, 7",
            "BOTTOM_CENTER, true,  true,   0, 300, 0, 7",
            "BOTTOM_CENTER, false, false, 24,   0, 0, 31",
            "BOTTOM_CENTER, true,  false, 24, 300, 0, 31",
            "TOP_CENTER,    false, true,  24,   0, 39, 0",
            "TOP_CENTER,    true,  true,  24, 300, 39, 0"
        })
        void appliesOnlyRemainingSafeAreaInsets(
            String position,
            boolean keyboardVisible,
            boolean systemBarsHandlesInsets,
            int safeBottom,
            int imeBottom,
            int expectedTop,
            int expectedBottom
        ) {
            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().setPosition(position).setMargin(7).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);
            sut.showBanner(mock(PluginCall.class), systemBarsHandlesInsets);

            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            ArgumentCaptor<View.OnApplyWindowInsetsListener> listener = ArgumentCaptor.forClass(View.OnApplyWindowInsetsListener.class);
            verify(banner).setOnApplyWindowInsetsListener(listener.capture());
            verify(viewGroupMock, never()).setOnApplyWindowInsetsListener(any());
            WindowInsets platformInsets = mock(WindowInsets.class);
            WindowInsetsCompat compat = mock(WindowInsetsCompat.class);
            when(compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
                Insets.of(0, 32, 0, safeBottom)
            );
            when(compat.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(keyboardVisible);
            if (keyboardVisible) {
                when(compat.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, imeBottom));
            }
            try (MockedStatic<WindowInsetsCompat> conversion = Mockito.mockStatic(WindowInsetsCompat.class)) {
                conversion.when(() -> WindowInsetsCompat.toWindowInsetsCompat(platformInsets, banner)).thenReturn(compat);
                clearInvocations(params);
                assertSame(platformInsets, listener.getValue().onApplyWindowInsets(banner, platformInsets));
                verify(params).setMargins(0, expectedTop, 0, expectedBottom);
            }
        }

        @ParameterizedTest
        @CsvSource({ "TOP_CENTER, 300, 0", "BOTTOM_CENTER, 0, 300", "CENTER, 0, 0" })
        void largeOffsetOnlyConsumesSpaceAtTheAnchoredEdge(String position, int top, int bottom) {
            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().setPosition(position).setMargin(300).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);
            sut.showBanner(mock(PluginCall.class), true);
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            verify(params).setMargins(0, top, 0, bottom);
        }

        @Test
        void restoresMarginsAfterKeyboardClosesWithoutAccumulatingInsets() {
            adOptionsMockForTesting = new AdOptions.TesterAdOptionsBuilder().setPosition("BOTTOM_CENTER").setMargin(7).build();
            when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsMockForTesting);
            sut.showBanner(mock(PluginCall.class), true);
            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            CoordinatorLayout.LayoutParams params = layoutParamsMockedConstruction.constructed().get(0);
            ArgumentCaptor<View.OnApplyWindowInsetsListener> listener = ArgumentCaptor.forClass(View.OnApplyWindowInsetsListener.class);
            verify(banner).setOnApplyWindowInsetsListener(listener.capture());
            WindowInsets platformInsets = mock(WindowInsets.class);
            WindowInsetsCompat compat = mock(WindowInsetsCompat.class);
            when(compat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout())).thenReturn(
                Insets.of(0, 32, 0, 24)
            );
            when(compat.isVisible(WindowInsetsCompat.Type.ime())).thenReturn(false, true, false, false);
            when(compat.getInsets(WindowInsetsCompat.Type.ime())).thenReturn(Insets.of(0, 0, 0, 300));
            try (MockedStatic<WindowInsetsCompat> conversion = Mockito.mockStatic(WindowInsetsCompat.class)) {
                conversion.when(() -> WindowInsetsCompat.toWindowInsetsCompat(platformInsets, banner)).thenReturn(compat);
                clearInvocations(params);
                for (int i = 0; i < 4; i++) {
                    listener.getValue().onApplyWindowInsets(banner, platformInsets);
                }
                var order = inOrder(params);
                order.verify(params).setMargins(0, 0, 0, 31);
                order.verify(params).setMargins(0, 0, 0, 7);
                order.verify(params, times(2)).setMargins(0, 0, 0, 31);
            }
        }

        @Test
        void requestsInsetsAfterAttachingBanner() {
            sut.showBanner(mock(PluginCall.class), true);
            RelativeLayout banner = relativeLayoutMockedConstruction.constructed().get(0);
            verify(activityMock, atLeast(1)).runOnUiThread(runnableArgumentCaptor.capture());
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
            runImmediatelyOnUiThread();

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
