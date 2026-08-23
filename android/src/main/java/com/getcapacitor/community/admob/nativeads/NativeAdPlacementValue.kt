package com.getcapacitor.community.admob.nativeads

import org.json.JSONObject
import kotlin.math.roundToInt

internal class NativeAdPlacementValue private constructor(
    val feedId: String,
    val slotKey: String,
    val generation: Int,
    val rectX: Int,
    val rectY: Int,
    val rectWidth: Int,
    val rectHeight: Int,
    val clipX: Int,
    val clipY: Int,
    val clipWidth: Int,
    val clipHeight: Int,
) {
    private fun hasArea(): Boolean = rectWidth > 0 && rectHeight > 0 && clipWidth > 0 && clipHeight > 0

    companion object {
        fun parse(expectedFeedId: String, value: JSONObject, density: Float): NativeAdPlacementValue? {
            if (!value.optBoolean("visible", false)) return null
            val rect = value.getJSONObject("rect")
            val clip = value.getJSONObject("clipRect")
            val placement = NativeAdPlacementValue(
                feedId = value.getString("feedId"),
                slotKey = value.getString("slotKey"),
                generation = value.optInt("generation", -1),
                rectX = rect.pixels("x", density),
                rectY = rect.pixels("y", density),
                rectWidth = rect.pixels("width", density),
                rectHeight = rect.pixels("height", density),
                clipX = clip.pixels("x", density),
                clipY = clip.pixels("y", density),
                clipWidth = clip.pixels("width", density),
                clipHeight = clip.pixels("height", density),
            )
            return placement.takeIf { it.feedId == expectedFeedId && it.hasArea() }
        }

        private fun JSONObject.pixels(name: String, density: Float): Int = (getDouble(name) * density).roundToInt()
    }
}
