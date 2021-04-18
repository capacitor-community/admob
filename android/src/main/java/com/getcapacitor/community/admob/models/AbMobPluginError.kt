package com.getcapacitor.community.admob.models

import com.getcapacitor.JSObject
import com.google.android.gms.ads.AdError

data class AbMobPluginError(val code: Int, val reason: String) : JSObject() {
    override fun put(key: String, value: Int): JSObject {
        throw Exception("Do not put elements directly here use the constructor")
    }
    init {
        super.put("code", this.code)
        super.put("reason", this.reason)
    }
    constructor(adError: AdError): this(adError.code, adError.message)
}
