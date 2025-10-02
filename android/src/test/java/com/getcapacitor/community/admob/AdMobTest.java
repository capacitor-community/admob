package com.getcapacitor.community.admob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.getcapacitor.JSArray;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.banner.BannerExecutor;
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

    @Mock
    MockedConstruction<BannerExecutor> bannerExecutorMockedConstruction;

    AdMob sut;

    @BeforeEach
    public void beforeEach() {
        reset(pluginCallMock, mockedContext);

        sut = new AdMob() {
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

    @AfterEach
    public void afterEach() {
        bannerExecutorMockedConstruction.close();
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

            mobileAdsMockedStatic.verify(() -> MobileAds.setRequestConfiguration(argumentCaptor.capture()), times(1));
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

            mobileAdsMockedStatic.verify(() -> MobileAds.setRequestConfiguration(argumentCaptor.capture()), times(1));
            try {
                assertEquals(testingDevices.toList(), argumentCaptor.getValue().getTestDeviceIds());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Initializes the banner executor")
        public void bannerExecutorInitialize() {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(false);

            sut.initialize(pluginCallMock);

            BannerExecutor bannerExecutor = bannerExecutorMockedConstruction.constructed().get(0);
            verify(bannerExecutor).initialize();
        }
    }
}
