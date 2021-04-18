package com.getcapacitor.community.admob.rewarded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.community.admob.rewarded.AdRewardExecutor;
import com.google.android.gms.common.util.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdRewardExecutorTest {

    @Mock
    Context context;

    @Mock
    Activity activity;

    @Mock
    BiConsumer<String, JSObject> notifierMock;

    final String LOG_TAG = "AdRewardHandlerTest Log Tag";

    AdRewardExecutor sut;

    @BeforeEach
    void beforeEach() {
        reset(context, activity, notifierMock);
        sut = new AdRewardExecutor(() -> context, () -> activity, notifierMock, LOG_TAG);
    }



    @Nested
    class Listeners {

    }
}
