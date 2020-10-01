package com.getcapacitor.community.admob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.getcapacitor.JSArray;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.AdSizeEnum;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import org.json.JSONException;
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
public class AdMobTest {
    @Mock
    Context mockedContext;

    @Mock
    AppCompatActivity mockedActivity;

    @Mock
    PluginCall pluginCallMock;

    AdMob sut;

    @BeforeEach
    public void beforeEach() {
        sut =
            new AdMob() {

                @Override
                public Context getContext() {
                    return mockedContext;
                }

                @Override
                public AppCompatActivity getActivity() {
                    return mockedActivity;
                }

                @Override
                public String getLogTag() {
                    return "LogTag";
                }
            };
    }

    @Nested
    @DisplayName("Initialize()")
    class Initialize {
        MockedStatic<MobileAds> mobileAdsMockedStatic;
        JSArray testingDevices;

        ArgumentCaptor<RequestConfiguration> argumentCaptor;

        @BeforeEach
        void beforeEachInitializeTest() {
            mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
            argumentCaptor = ArgumentCaptor.forClass(RequestConfiguration.class);
        }

        @AfterEach
        void afterEachInitializeTest() {
            mobileAdsMockedStatic.close();
        }

        @Test
        @DisplayName("If we initialize in not testing mode, then set the testing devices to an empty list")
        public void emptyTestingDevices() {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(false);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.initialize(pluginCallMock);

            mobileAdsMockedStatic.verify(times(1), () -> MobileAds.setRequestConfiguration(argumentCaptor.capture()));
            assertEquals(0, argumentCaptor.getValue().getTestDeviceIds().size());
        }

        @Test
        @DisplayName("Register Testing Devices if in testing Mode")
        public void registerTestingDevices() {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(true);
            testingDevices = new JSArray();
            testingDevices.put("One");
            testingDevices.put("Two");
            when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(testingDevices);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.initialize(pluginCallMock);

            mobileAdsMockedStatic.verify(times(1), () -> MobileAds.setRequestConfiguration(argumentCaptor.capture()));
            try {
                assertEquals(testingDevices.toList(), argumentCaptor.getValue().getTestDeviceIds());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("Ads Creation")
    class AdsCreation {
        AdOptions adOptionsWithNpaTrue = AdOptions
            .getFactory()
            ._createTesterAdOptions("The Id", "The Testing ID", false, "TOP", 0, true, AdSizeEnum.SMART_BANNER);
        AdOptions adOptionsWithNpaFalse = AdOptions
            .getFactory()
            ._createTesterAdOptions("The Id", "The Testing ID", false, "TOP", 0, false, AdSizeEnum.SMART_BANNER);

        MockedStatic<MobileAds> mobileAdsMockedStatic;
        MockedConstruction<AdView> adViewMockedConstruction;
        MockedStatic<AdOptions> adOptionsStaticMocked;
        MockedStatic<AdViewIdHelper> adViewIdHelperMockedStatic;
        MockedConstruction<AdRequest.Builder> adRequestBuilderMockedConstruction;
        MockedConstruction<Bundle> bundleMockedConstruction;
        MockedConstruction<CoordinatorLayout.LayoutParams> layoutParamsMockedConstruction;

        @Mock
        AdOptions.AdOptionsFactory adOptionsFactoryMock;

        ArgumentCaptor<Runnable> runnableArgumentCaptor;

        @BeforeEach
        void beforeEachAdCreation() {
            reset(pluginCallMock, adOptionsFactoryMock);
            runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);

            mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
            adViewMockedConstruction = Mockito.mockConstruction(AdView.class);
            adRequestBuilderMockedConstruction = Mockito.mockConstruction(AdRequest.Builder.class);

            bundleMockedConstruction = Mockito.mockConstruction(Bundle.class);

            layoutParamsMockedConstruction = Mockito.mockConstruction(CoordinatorLayout.LayoutParams.class);
            adViewIdHelperMockedStatic = Mockito.mockStatic(AdViewIdHelper.class);

            adOptionsStaticMocked = Mockito.mockStatic(AdOptions.class);
            adOptionsStaticMocked.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);
        }

        @AfterEach
        void afterEachAdCreation() {
            mobileAdsMockedStatic.close();
            adViewMockedConstruction.close();
            adOptionsStaticMocked.close();
            adRequestBuilderMockedConstruction.close();
            bundleMockedConstruction.close();
            layoutParamsMockedConstruction.close();
            adViewIdHelperMockedStatic.close();
        }

        @Nested
        @DisplayName("Non Personalized Ads")
        class NonPersonalizedAds {

            @BeforeEach
            public void beforeEach() {
                final ViewGroup viewGroupMock = mock(ViewGroup.class);
                when(mockedActivity.findViewById(anyInt())).thenReturn(viewGroupMock);
                when(viewGroupMock.getChildAt(anyInt())).thenReturn(viewGroupMock);

                sut.initialize(pluginCallMock);
                lenient().when(adOptionsFactoryMock.createInterstitialOptions(any())).thenReturn(adOptionsWithNpaTrue);
                lenient().when(adOptionsFactoryMock.createRewardVideoOptions(any())).thenReturn(adOptionsWithNpaTrue);
                lenient().when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(new JSArray());
            }

            @Test
            void banner() {
                try (MockedConstruction<RelativeLayout> relativeLayoutMockedConstruction = Mockito.mockConstruction(RelativeLayout.class)) {
                    when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsWithNpaTrue);
                    Resources mockedResourcesMock = mock(Resources.class);
                    DisplayMetrics displayMetricsMock = mock(DisplayMetrics.class);
                    displayMetricsMock.density = 1f;
                    when(mockedContext.getResources()).thenReturn(mockedResourcesMock);
                    when(mockedResourcesMock.getDisplayMetrics()).thenReturn(displayMetricsMock);

                    sut.showBanner(pluginCallMock);
                    verify(mockedActivity).runOnUiThread(runnableArgumentCaptor.capture());
                    Runnable uiThreadRunnable = runnableArgumentCaptor.getValue();
                    uiThreadRunnable.run();

                    Bundle mockedBundle = bundleMockedConstruction.constructed().get(0);
                    AdRequest.Builder adRequestBuilder = adRequestBuilderMockedConstruction.constructed().get(0);
                    verify(mockedBundle).putString("npa", "1");
                    verify(adRequestBuilder).addNetworkExtrasBundle(AdMobAdapter.class, mockedBundle);
                }
            }
        }
    }
}
