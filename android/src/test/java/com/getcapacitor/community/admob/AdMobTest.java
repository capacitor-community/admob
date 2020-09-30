package com.getcapacitor.community.admob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.getcapacitor.JSArray;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.AdSizeEnum;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdMobTest {
    AdOptions adOptionsStub;

    @Mock
    PluginCall pluginCallMock;

    @Mock
    AdOptions.AdOptionsFactory adOptionsFactoryMock;

    @Mock
    Context mockedContext;

    AdMob sut;

    @BeforeEach
    public void beforeEach() {
        adOptionsStub =
            AdOptions.getFactory()._createTesterAdOptions("The Id", "The Testing ID", true, "TOP", 0, false, AdSizeEnum.SMART_BANNER);
        MockitoAnnotations.openMocks(this);
        reset(pluginCallMock, adOptionsFactoryMock);

        lenient().when(adOptionsFactoryMock.createBannerOptions(any())).thenReturn(adOptionsStub);
        lenient().when(adOptionsFactoryMock.createInterstitialOptions(any())).thenReturn(adOptionsStub);
        lenient().when(adOptionsFactoryMock.createRewardVideoOptions(any())).thenReturn(adOptionsStub);
        lenient().when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(new JSArray());

        sut =
            new AdMob() {

                @Override
                public Context getContext() {
                    return mockedContext;
                }
            };
    }

    @Nested
    @DisplayName("Testing Devices")
    class TestingDevices {
        final AdOptions adOptionsWithIsTestingTrue = AdOptions
            .getFactory()
            ._createTesterAdOptions("The Id", "The Testing ID", true, "TOP", 0, true, AdSizeEnum.SMART_BANNER);
        final AdOptions adOptionsWithIsTestingFalse = AdOptions
            .getFactory()
            ._createTesterAdOptions("The Id", "The Testing ID", false, "TOP", 0, true, AdSizeEnum.SMART_BANNER);
        MockedStatic<MobileAds> mobileAdsMockedStatic;
        MockedConstruction<AdView> adViewMocked;
        MockedStatic<AdOptions> adOptionsStaticMocked;
        JSArray testingDevices;

        ArgumentCaptor<RequestConfiguration> argumentCaptor;

        @BeforeEach
        void beforeEachNpaTest() {
            mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
            adViewMocked = Mockito.mockConstruction(AdView.class);

            adOptionsStaticMocked = Mockito.mockStatic(AdOptions.class);
            adOptionsStaticMocked.when(AdOptions::getFactory).thenReturn(adOptionsFactoryMock);

            testingDevices = new JSArray();
            testingDevices.put("One");
            testingDevices.put("Two");

            when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(testingDevices);

            argumentCaptor = ArgumentCaptor.forClass(RequestConfiguration.class);
        }

        @AfterEach
        void afterEachNpaTest() {
            mobileAdsMockedStatic.close();
            adViewMocked.close();
            adOptionsStaticMocked.close();
        }

        @Test
        public void Register_Testing_Devices() {
            final JSArray userGivenDevices = new JSArray();
            when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(userGivenDevices);

            sut.initialize(pluginCallMock);

            assertEquals(sut.testingDevices, userGivenDevices);
        }

        @Test
        @DisplayName("Banner Testing Devices")
        public void showBanner() {
            sut.initialize(pluginCallMock);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.showBanner(pluginCallMock);

            mobileAdsMockedStatic.verify(times(1), () -> MobileAds.setRequestConfiguration(argumentCaptor.capture()));
            try {
                assertEquals(testingDevices.toList(), argumentCaptor.getValue().getTestDeviceIds());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Interstitial Testing Devices")
        public void prepareInterstitial() {
            sut.initialize(pluginCallMock);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.prepareInterstitial(pluginCallMock);

            mobileAdsMockedStatic.verify(times(1), () -> MobileAds.setRequestConfiguration(argumentCaptor.capture()));
            try {
                assertEquals(testingDevices.toList(), argumentCaptor.getValue().getTestDeviceIds());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Rewarded Ad Testing Devices")
        public void prepareRewardedAd() {
            sut.initialize(pluginCallMock);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.prepareRewardVideoAd(pluginCallMock);

            mobileAdsMockedStatic.verify(times(1), () -> MobileAds.setRequestConfiguration(argumentCaptor.capture()));
            try {
                assertEquals(testingDevices.toList(), argumentCaptor.getValue().getTestDeviceIds());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
