package com.getcapacitor.community.admob.helpers;

import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Tracks an ad's lifetime using a monotonic clock that includes device sleep. */
public final class AdExpiration {

    private final LongSupplier clock;
    private final long loadedAt;
    private final long maxAge;

    public AdExpiration(long maxAgeSeconds) {
        this(maxAgeSeconds, SystemClock::elapsedRealtimeNanos);
    }

    public AdExpiration(long maxAgeSeconds, LongSupplier clock) {
        this.clock = clock;
        this.loadedAt = clock.getAsLong();
        this.maxAge = TimeUnit.SECONDS.toNanos(maxAgeSeconds);
    }

    public boolean isExpired() {
        long age = clock.getAsLong() - loadedAt;
        return age < 0 || age >= maxAge;
    }
}
