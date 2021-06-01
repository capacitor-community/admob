package com.getcapacitor.community.admob.banner

import com.google.android.gms.ads.AdSize

/**
 * https://developers.google.com/admob/android/banner#banner_sizes
 */
enum class BannerAdSizeEnum(val size: AdSize) {
    BANNER(AdSize.BANNER),
    LARGE_BANNER(AdSize.LARGE_BANNER),
    MEDIUM_RECTANGLE(AdSize.MEDIUM_RECTANGLE),
    FULL_BANNER(AdSize.FULL_BANNER),
    LEADERBOARD(AdSize.LEADERBOARD),
    ADAPTIVE_BANNER(AdSize.INVALID),
    SMART_BANNER(AdSize.zza);

    override fun toString(): String {
        return name
    }
}