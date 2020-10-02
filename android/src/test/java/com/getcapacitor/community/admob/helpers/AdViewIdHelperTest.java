package com.getcapacitor.community.admob.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.gms.ads.AdRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdViewIdHelperTest {
    @Mock
    Context contextMock;

    MockedStatic<Log> logMockedStatic;

    @BeforeEach
    void setUp() {
        logMockedStatic = Mockito.mockStatic(Log.class);
    }

    @AfterEach
    void tearDown() {
        logMockedStatic.close();
    }

    @Nested
    @DisplayName("#getFinalAdId()")
    class GeFinalAdId {

        @Test
        @DisplayName("Returns the real adId if the adOptions is not for testing")
        void notAdOptionsForTesting() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(false).build();

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, mock(AdRequest.class), "test", contextMock);

            assertEquals(adOptions.adId, returnedId);
        }

        @Test
        @DisplayName("Returns the real adId if the adOptions is for testing but we are on a registered testing device")
        void testingWithATestingDevice() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(true).build();
            final AdRequest adRequest = mock(AdRequest.class);
            when(adRequest.isTestDevice(any())).thenReturn(true);

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, adRequest, "test", contextMock);

            assertEquals(adOptions.adId, returnedId);
        }

        @Test
        @DisplayName("Returns the testingId when options are for testing and we are not in a testing device")
        void testingWithoutTestingDevice() {
            final AdOptions adOptions = new AdOptions.TesterAdOptionsBuilder().setIsTesting(true).build();
            final AdRequest adRequest = mock(AdRequest.class);
            when(adRequest.isTestDevice(any())).thenReturn(false);

            final String returnedId = AdViewIdHelper.getFinalAdId(adOptions, adRequest, "test", contextMock);

            assertEquals(adOptions.getTestingId(), returnedId);
        }
    }
}
