package com.getcapacitor.community.admob.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdViewIdHelperTest {

    @Mock
    Context contextMock;

    MockedStatic<Log> logMockedStatic;
    MockedStatic<MobileAds> mobileAdsMockedStatic;

    @BeforeEach
    void setUp() {
        logMockedStatic = Mockito.mockStatic(Log.class);
        mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
    }

    @AfterEach
    void tearDown() {
        logMockedStatic.close();
        mobileAdsMockedStatic.close();
    }

    @Nested
    @DisplayName("#getFinalAdId()")
    class GeFinalAdId {

        @Test
        @DisplayName("Returns the real adId if the adOptions is not for testing")
        void notAdOptionsForTesting() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(false).build();

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, "test", contextMock);

            assertEquals(adOptions.adId, returnedId);
        }

        @Test
        @DisplayName("Returns the real adId if the adOptions is for testing but we are on a registered testing device")
        void testingWithATestingDevice() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(true).build();
            final RequestConfiguration requestConfiguration = mock(RequestConfiguration.class);
            mobileAdsMockedStatic.when(MobileAds::getRequestConfiguration).thenReturn(requestConfiguration);
            Mockito.when(requestConfiguration.isTestDevice(contextMock)).thenReturn(true);

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, "test", contextMock);

            assertEquals(adOptions.adId, returnedId);
        }

        @Test
        @DisplayName("Returns the testingId when options are for testing and we are not in a testing device")
        void testingWithoutTestingDevice() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(true).build();
            final RequestConfiguration requestConfiguration = mock(RequestConfiguration.class);
            mobileAdsMockedStatic.when(MobileAds::getRequestConfiguration).thenReturn(requestConfiguration);
            Mockito.when(requestConfiguration.isTestDevice(contextMock)).thenReturn(false);

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, "test", contextMock);

            assertEquals(adOptions.getTestingId(), returnedId);
        }
    }
}
