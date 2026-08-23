package com.getcapacitor.community.admob.nativeads;

import org.json.JSONException;
import org.json.JSONObject;

final class NativeAdPlacementValue {

    final String feedId;
    final String slotKey;
    final int generation;
    final int rectX;
    final int rectY;
    final int rectWidth;
    final int rectHeight;
    final int clipX;
    final int clipY;
    final int clipWidth;
    final int clipHeight;

    private NativeAdPlacementValue(JSONObject value, float density) throws JSONException {
        feedId = value.getString("feedId");
        slotKey = value.getString("slotKey");
        generation = value.optInt("generation", -1);
        JSONObject rect = value.getJSONObject("rect");
        JSONObject clip = value.getJSONObject("clipRect");
        rectX = toPixels(rect.getDouble("x"), density);
        rectY = toPixels(rect.getDouble("y"), density);
        rectWidth = toPixels(rect.getDouble("width"), density);
        rectHeight = toPixels(rect.getDouble("height"), density);
        clipX = toPixels(clip.getDouble("x"), density);
        clipY = toPixels(clip.getDouble("y"), density);
        clipWidth = toPixels(clip.getDouble("width"), density);
        clipHeight = toPixels(clip.getDouble("height"), density);
    }

    static NativeAdPlacementValue parse(String expectedFeedId, JSONObject value, float density) throws JSONException {
        if (!value.optBoolean("visible", false)) return null;
        NativeAdPlacementValue placement = new NativeAdPlacementValue(value, density);
        if (!expectedFeedId.equals(placement.feedId) || !placement.hasArea()) return null;
        return placement;
    }

    private boolean hasArea() {
        return rectWidth > 0 && rectHeight > 0 && clipWidth > 0 && clipHeight > 0;
    }

    private static int toPixels(double logicalPixels, float density) {
        return Math.round((float) logicalPixels * density);
    }
}
