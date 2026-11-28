package com.getcapacitor.community.admob.nativeads

import io.github.webview_native_ui.engine.NativeUIPixelFrame
import org.json.JSONObject

internal class NativeAdPlacementValue private constructor(
    val feedId: String,
    val slotKey: String,
    val generation: Int,
    val rect: NativeUIPixelFrame,
    val clip: NativeUIPixelFrame,
) {
    companion object {
        fun parse(expectedFeedId: String, value: JSONObject, density: Float): NativeAdPlacementValue? {
            if (!value.optBoolean("visible", false)) return null
            val rect = NativeUIPixelFrame.fromCss(value.optJSONObject("rect"), density) ?: return null
            val clip = NativeUIPixelFrame.fromCss(value.optJSONObject("clipRect"), density) ?: return null
            if (value.optString("feedId") != expectedFeedId) return null
            if (rect.width == 0 || rect.height == 0 || clip.width == 0 || clip.height == 0) return null
            return NativeAdPlacementValue(expectedFeedId, value.getString("slotKey"), value.optInt("generation", -1), rect, clip)
        }
    }
}
