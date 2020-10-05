package com.getcapacitor.community.admob.models;

import com.google.android.gms.ads.AdSize;

public enum AdSizeEnum {
    BANNER("BANNER", AdSize.BANNER),
    FULL_BANNER("FULL_BANNER", AdSize.FULL_BANNER),
    LARGE_BANNER("LARGE_BANNER", AdSize.LARGE_BANNER),
    LEADERBOARD("LEADERBOARD", AdSize.LEADERBOARD),
    MEDIUM_RECTANGLE("MEDIUM_RECTANGLE", AdSize.MEDIUM_RECTANGLE),
    WIDE_SKYSCRAPER("WIDE_SKYSCRAPER", AdSize.WIDE_SKYSCRAPER),
    SMART_BANNER("SMART_BANNER", AdSize.SMART_BANNER),
    FLUID("FLUID", AdSize.FLUID);

    public final String name;
    public final AdSize size;

    AdSizeEnum(final String value, final AdSize size) {
        this.name = value;
        this.size = size;
    }

    @Override
    public String toString() {
        return name;
    }
}
