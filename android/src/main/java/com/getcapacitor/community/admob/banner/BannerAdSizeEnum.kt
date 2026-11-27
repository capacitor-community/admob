package com.getcapacitor.community.admob.banner

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize

/**
 * https://developers.google.com/admob/android/banner#banner_sizes
 */
enum class BannerAdSizeEnum(val size: AdSize) {
    BANNER(AdSize.BANNER),
    FULL_BANNER(AdSize.FULL_BANNER),
    LARGE_BANNER(AdSize.LARGE_BANNER),
    MEDIUM_RECTANGLE(AdSize.MEDIUM_RECTANGLE),
    LEADERBOARD(AdSize.LEADERBOARD),
    ADAPTIVE_BANNER(AdSize.BANNER), // Calculated dynamically in BannerExecutor.
    SMART_BANNER(AdSize.BANNER); // Kept for backward compatibility; handled as adaptive.

    override fun toString(): String {
        return name
    }
}