package com.getcapacitor.community.admob.consent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.common.util.BiConsumer;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdConsentExecutorTest {

    private static final String LOG_TAG = "AdConsentExecutorTest Log Tag";

    @Mock
    private Context contextMock;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private Activity activityMock;

    @Mock
    private BiConsumer<String, JSObject> notifierMock;

    @Mock
    private ConsentInformation mockedConsentInformation;

    private MockedStatic<UserMessagingPlatform> mockedUserMessagingPlatform;

    private ArgumentCaptor<ConsentForm.OnConsentFormDismissedListener> listenerCaptor;

    @Mock
    private PluginCall pluginCallMock;

    private AdConsentExecutor adConsentExecutor;

    @BeforeEach
    void beforeEach() {
        mockedUserMessagingPlatform = Mockito.mockStatic(UserMessagingPlatform.class);
        mockedUserMessagingPlatform
            .when(() -> UserMessagingPlatform.getConsentInformation(any(Context.class)))
            .thenReturn(mockedConsentInformation);
        listenerCaptor = ArgumentCaptor.forClass(ConsentForm.OnConsentFormDismissedListener.class);

        adConsentExecutor = new AdConsentExecutor(() -> contextMock, () -> activityMock, notifierMock, LOG_TAG);

        doAnswer((invocation) -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        })
            .when(activityMock)
            .runOnUiThread(any(Runnable.class));
    }

    @AfterEach
    void afterEach() {
        mockedUserMessagingPlatform.close();
    }

    @Nested
    @DisplayName("Show Privacy Options Form Tests")
    class ShowPrivacyOptionsFormTests {

        @Test
        @DisplayName("should resolve the call on success")
        void showPrivacyOptionsForm_Success() {
            adConsentExecutor.showPrivacyOptionsForm(pluginCallMock, null);
            mockedUserMessagingPlatform.verify(() ->
                UserMessagingPlatform.showPrivacyOptionsForm(eq(activityMock), listenerCaptor.capture())
            );
            listenerCaptor.getValue().onConsentFormDismissed(null);
            verify(pluginCallMock).resolve();
        }

        @Test
        @DisplayName("should reject the call on failure")
        void showPrivacyOptionsForm_Failure() {
            FormError testError = new FormError(123, "Test privacy form error");
            adConsentExecutor.showPrivacyOptionsForm(pluginCallMock, null);
            mockedUserMessagingPlatform.verify(() ->
                UserMessagingPlatform.showPrivacyOptionsForm(eq(activityMock), listenerCaptor.capture())
            );
            listenerCaptor.getValue().onConsentFormDismissed(testError);
            verify(pluginCallMock).reject("Error when show privacy form", testError.getMessage());
        }

        @Test
        @DisplayName("should reject the call if activity is null")
        void showPrivacyOptionsForm_NullActivity() {
            adConsentExecutor = new AdConsentExecutor(() -> contextMock, () -> null, notifierMock, LOG_TAG);
            adConsentExecutor.showPrivacyOptionsForm(pluginCallMock, null);
            verify(pluginCallMock).reject("Trying to show the privacy options form but the Activity is null");
        }
    }
}
