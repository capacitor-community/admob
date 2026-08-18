package com.getcapacitor.community.admob.models

import com.getcapacitor.JSObject
import com.google.android.libraries.ads.mobile.sdk.common.AdValue

class AdMobRevenueData(adValue: AdValue, adUnitId: String, networkName: String, impressionId: String) : JSObject() {
    init {
        put("adUnitId", adUnitId)
        put("valueMicros", adValue.valueMicros)
        put("currencyCode", adValue.currencyCode)
        put("precision", adValue.precisionType)
        put("networkName", networkName)
        put("impressionId", impressionId)
    }
}
