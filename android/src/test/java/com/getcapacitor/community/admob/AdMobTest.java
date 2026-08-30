package com.getcapacitor.community.admob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import com.getcapacitor.JSArray;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.banner.BannerExecutor;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdMobTest {

    @Mock
    Context mockedContext;

    @Mock
    AppCompatActivity mockedActivity;

    @Mock
    PluginCall pluginCallMock;

    @Mock
    MockedConstruction<BannerExecutor> bannerExecutorMockedConstruction;

    @Mock
    PackageManager packageManagerMock;

    @Mock
    ApplicationInfo applicationInfoMock;

    @Mock
    Resources resourcesMock;

    AdMob sut;

    @BeforeEach
    public void beforeEach() {
        reset(pluginCallMock, mockedContext);
        when(mockedContext.getPackageManager()).thenReturn(packageManagerMock);
        when(mockedContext.getPackageName()).thenReturn("test.package");
        when(mockedContext.getResources()).thenReturn(resourcesMock);

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

        ArgumentCaptor<InitializationConfig> argumentCaptor;

        @BeforeEach
        void beforeEachInitializeTest() {
            mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
            argumentCaptor = ArgumentCaptor.forClass(InitializationConfig.class);
        }

        @AfterEach
        void afterEachInitializeTest() {
            mobileAdsMockedStatic.close();
        }

        @Test
        @DisplayName("If we initialize in not testing mode, then set the testing devices to an empty list")
        public void emptyTestingDevices() throws Exception {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(false);
            final ApplicationInfo applicationInfo = new ApplicationInfo();
            final android.os.Bundle metaData = Mockito.mock(android.os.Bundle.class);
            when(metaData.get("com.google.android.gms.ads.APPLICATION_ID")).thenReturn("test-app-id");
            applicationInfo.metaData = metaData;
            doReturn(applicationInfo).when(packageManagerMock).getApplicationInfo("test.package", PackageManager.GET_META_DATA);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.initialize(pluginCallMock);
            assertEquals(0, argumentCaptor.getAllValues().size());
        }

        @Test
        @DisplayName("Register Testing Devices if in testing Mode")
        public void registerTestingDevices() throws Exception {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(true);
            final ApplicationInfo applicationInfo = new ApplicationInfo();
            final android.os.Bundle metaData = Mockito.mock(android.os.Bundle.class);
            when(metaData.get("com.google.android.gms.ads.APPLICATION_ID")).thenReturn("test-app-id");
            applicationInfo.metaData = metaData;
            doReturn(applicationInfo).when(packageManagerMock).getApplicationInfo("test.package", PackageManager.GET_META_DATA);
            testingDevices = new JSArray();
            testingDevices.put("One");
            testingDevices.put("Two");
            when(pluginCallMock.getArray("testingDevices", AdMob.EMPTY_TESTING_DEVICES)).thenReturn(testingDevices);
            assertEquals(argumentCaptor.getAllValues().size(), 0); // Correct env

            sut.initialize(pluginCallMock);
            assertEquals(0, argumentCaptor.getAllValues().size());
        }

        @Test
        @DisplayName("Initializes the banner executor")
        public void bannerExecutorInitialize() throws Exception {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(false);
            final ApplicationInfo applicationInfo = new ApplicationInfo();
            final android.os.Bundle metaData = Mockito.mock(android.os.Bundle.class);
            when(metaData.get("com.google.android.gms.ads.APPLICATION_ID")).thenReturn("test-app-id");
            applicationInfo.metaData = metaData;
            doReturn(applicationInfo).when(packageManagerMock).getApplicationInfo("test.package", PackageManager.GET_META_DATA);
            doAnswer((invocation) -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            })
                .when(mockedActivity)
                .runOnUiThread(any(Runnable.class));

            sut.initialize(pluginCallMock);
            assertEquals("test.package", mockedContext.getPackageName());
        }
    }
}
