package com.getcapacitor.community.admob.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.getcapacitor.PluginCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdOptionsTest {
    @Mock
    PluginCall pluginCallMock;

    @BeforeEach
    public void beforeEach() {
        reset(pluginCallMock);
        // Dummy Returns
        when(pluginCallMock.getString(anyString(), anyString())).thenReturn("DEFAULT_RETURN");
    }

    // Just for now lets check the call and default value in the same tests.
    @Nested
    @DisplayName("Gets Property from plugin call or default")
    class GetProperty {

        @Test
        public void ad_Id() {
            final String expected = "Some Given Test Id";
            when(pluginCallMock.getString(eq("adId"), anyString())).thenReturn(expected);

            final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");

            assertEquals(expected, adOptions.adId);
        }

        @Test
        public void position() {
            final String wantedProperty = "position";
            final String expected = "Some Position";
            final String defaultValue = "BOTTOM_CENTER";
            when(pluginCallMock.getString(eq(wantedProperty), anyString())).thenReturn(expected);

            final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");

            verify(pluginCallMock).getString(wantedProperty, defaultValue);
            assertEquals(expected, adOptions.position);
        }

        @Test
        public void margin() {
            final String wantedProperty = "margin";
            final int expected = 69;
            final int defaultValue = 0;
            when(pluginCallMock.getInt(eq(wantedProperty), anyInt())).thenReturn(expected);

            final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");

            verify(pluginCallMock).getInt(wantedProperty, defaultValue);
            assertEquals(expected, adOptions.margin);
        }

        @Test
        public void isTesting() {
            final String wantedProperty = "isTesting";
            final boolean expected = true;
            final boolean defaultValue = false;
            when(pluginCallMock.getBoolean(eq(wantedProperty), anyBoolean())).thenReturn(expected);

            final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");

            verify(pluginCallMock).getBoolean(wantedProperty, defaultValue);
            assertEquals(expected, adOptions.isTesting);
        }

        @Test
        public void npa() {
            final String wantedProperty = "npa";
            final boolean expected = true;
            final boolean defaultValue = false;
            lenient().when(pluginCallMock.getBoolean(eq(wantedProperty), anyBoolean())).thenReturn(expected);

            final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");

            verify(pluginCallMock).getBoolean(wantedProperty, defaultValue);
            assertEquals(expected, adOptions.npa);
        }
        //    @Test
        //    public void testingDevices() {
        //      final String wantedProperty = "testingDevices";
        //      final JSArray expected = new JSArray();
        //      expected.put("one").put("two");
        //      when(
        //          pluginCallMock.getArray(
        //            eq(wantedProperty),
        //            Mockito.any(JSArray.class)
        //          )
        //        )
        //        .thenReturn(expected);
        //
        //      final AdOptions adOptions = AdOptions.getFactory().createGenericOptions(pluginCallMock, "");
        //
        //      verify(pluginCallMock)
        //        .getArray(wantedProperty, AdOptions.getFactory().EMPTY_TESTING_DEVICES);
        //      assertEquals(expected, adOptions.testingDevices);
        //    }
    }
}
