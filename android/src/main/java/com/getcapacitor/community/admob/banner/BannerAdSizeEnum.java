package com.getcapacitor.community.admob.banner;

import com.google.android.gms.ads.AdSize;

/**
 * https://developers.google.com/admob/android/banner#banner_sizes
 */
public enum BannerAdSizeEnum {
    BANNER("BANNER", AdSize.BANNER),
    LARGE_BANNER("LARGE_BANNER", AdSize.LARGE_BANNER),
    MEDIUM_RECTANGLE("MEDIUM_RECTANGLE", AdSize.MEDIUM_RECTANGLE),
    FULL_BANNER("FULL_BANNER", AdSize.FULL_BANNER),
    LEADERBOARD("LEADERBOARD", AdSize.LEADERBOARD),
    ADAPTIVE_BANNER("ADAPTIVE_BANNER", AdSize.INVALID),
    SMART_BANNER("ADAPTIVE_BANNER", AdSize.zza);

    public final String name;
    public final AdSize size;

    BannerAdSizeEnum(final String value, final AdSize size) {
        this.name = value;
        this.size = size;
    }

    @Override
    public String toString() {
        return name;
    }
}
