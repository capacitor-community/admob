package com.getcapacitor.community.admob.nativeads

import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NativeAdPlacementValueTest {
    @Test
    fun rejectsWrongFeedAndMalformedRect() {
        val placement = JSONObject()
            .put("visible", true)
            .put("feedId", "feed")
            .put("slotKey", "slot")
            .put("generation", 1)
            .put("rect", rect(144, 300))
            .put("clipRect", rect(144, 300))

        assertNotNull(NativeAdPlacementValue.parse("feed", placement, 1f))
        assertNull(NativeAdPlacementValue.parse("another-feed", placement, 1f))
        placement.put("rect", rect(0, 300))
        assertNull(NativeAdPlacementValue.parse("feed", placement, 1f))
    }

    private fun rect(width: Int, height: Int): JSONObject = JSONObject()
        .put("x", 0)
        .put("y", 0)
        .put("width", width)
        .put("height", height)
}
