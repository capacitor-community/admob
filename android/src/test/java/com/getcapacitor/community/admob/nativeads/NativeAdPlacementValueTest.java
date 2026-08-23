package com.getcapacitor.community.admob.nativeads;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.JSONObject;
import org.junit.Test;

public class NativeAdPlacementValueTest {

    @Test
    public void rejectsWrongFeedAndMalformedRect() throws Exception {
        JSONObject placement = new JSONObject()
            .put("visible", true)
            .put("feedId", "feed")
            .put("slotKey", "slot")
            .put("generation", 1)
            .put("rect", rect(144, 300))
            .put("clipRect", rect(144, 300));

        assertNotNull(NativeAdPlacementValue.parse("feed", placement, 1));
        assertNull(NativeAdPlacementValue.parse("another-feed", placement, 1));
        placement.put("rect", rect(0, 300));
        assertNull(NativeAdPlacementValue.parse("feed", placement, 1));
    }

    private JSONObject rect(int width, int height) throws Exception {
        return new JSONObject().put("x", 0).put("y", 0).put("width", width).put("height", height);
    }
}
