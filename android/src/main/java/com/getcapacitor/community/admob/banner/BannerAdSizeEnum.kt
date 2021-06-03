package com.getcapacitor.community.admob.banner

import com.google.android.gms.ads.AdSize

/**
 * https://developers.google.com/admob/android/banner#banner_sizes
 */
enum class BannerAdSizeEnum(val size: AdSize) {
    BANNER(AdSize.BANNER),
    FULL_BANNER(AdSize.FULL_BANNER),
    LARGE_BANNER(AdSize.LARGE_BANNER),
    MEDIUM_RECTANGLE(AdSize.MEDIUM_RECTANGLE),
    LEADERBOARD(AdSize.LEADERBOARD),
    ADAPTIVE_BANNER(AdSize.INVALID), // We should not use the AdSize here but calculate the device size
    SMART_BANNER(AdSize.SMART_BANNER);

    override fun toString(): String {
        return name
    }
}