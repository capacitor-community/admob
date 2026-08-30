package com.getcapacitor.community.admob.banner

import com.getcapacitor.JSObject
import com.google.android.libraries.ads.mobile.sdk.banner.AdView

data class BannerAdSizeInfo(val width: Int, val height: Int) : JSObject() {
    override fun put(key: String, value: Int): JSObject {
        throw Exception("Do not put elements directly here use the constructor")
    }
    init {
        super.put("width", width)
        super.put("height", height)
    }
    constructor(mAdView: AdView): this(mAdView.width, mAdView.height)
}
