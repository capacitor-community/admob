package com.getcapacitor.community.admob.helpers;

import static org.junit.jupiter.api.Assertions.*;

import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AdExpirationTest {

    @ParameterizedTest
    @ValueSource(longs = { 3600, 14400 })
    void defaultClockCountsElapsedTimeDuringDeviceSleep(long seconds) {
        try (MockedStatic<SystemClock> clock = Mockito.mockStatic(SystemClock.class)) {
            clock.when(SystemClock::elapsedRealtimeNanos).thenReturn(100L);
            AdExpiration expiration = new AdExpiration(seconds);
            clock.when(SystemClock::elapsedRealtimeNanos).thenReturn(100L + TimeUnit.SECONDS.toNanos(seconds));
            assertTrue(expiration.isExpired());
        }
    }

    @ParameterizedTest
    @ValueSource(longs = { 3600, 14400 })
    void expiresAtTheBoundaryAndUsesTheSuccessfulLoadTime(long seconds) {
        AtomicLong clock = new AtomicLong(123);
        AdExpiration expiration = new AdExpiration(seconds, clock::get);
        clock.addAndGet(TimeUnit.SECONDS.toNanos(seconds) - 1);
        assertFalse(expiration.isExpired());
        clock.incrementAndGet();
        assertTrue(expiration.isExpired());
        AdExpiration reloaded = new AdExpiration(seconds, clock::get);
        assertFalse(reloaded.isExpired());
        clock.set(0);
        assertTrue(reloaded.isExpired());
    }
}
