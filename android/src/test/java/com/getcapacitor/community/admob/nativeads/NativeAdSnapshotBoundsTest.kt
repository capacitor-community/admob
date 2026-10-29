package com.getcapacitor.community.admob.nativeads

import org.junit.Assert.*
import org.junit.Test

class NativeAdSnapshotBoundsTest {
    @Test fun preservesTheTwoMillionPixelAllocationLimit() {
        assertTrue(NativeAdExecutor.isSnapshotSizeSupported(2000, 1000))
        assertFalse(NativeAdExecutor.isSnapshotSizeSupported(2001, 1000))
        assertFalse(NativeAdExecutor.isSnapshotSizeSupported(Int.MAX_VALUE, Int.MAX_VALUE))
        assertFalse(NativeAdExecutor.isSnapshotSizeSupported(0, 1000))
        assertFalse(NativeAdExecutor.isSnapshotSizeSupported(1000, -1))
    }
}
