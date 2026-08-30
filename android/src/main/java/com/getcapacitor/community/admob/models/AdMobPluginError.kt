package com.getcapacitor.community.admob.models

import com.getcapacitor.JSObject
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

data class AdMobPluginError(val code: Int, val message: String) : JSObject() {
    override fun put(key: String, value: Int): JSObject {
        throw Exception("Do not put elements directly here use the constructor")
    }
    init {
        super.put("code", this.code)
        super.put("message", this.message)
    }

    constructor(adError: LoadAdError) : this(codeToInt(adError.code), adError.message)

    constructor(fullScreenContentError: FullScreenContentError) : this(
        codeToInt(fullScreenContentError.code),
        fullScreenContentError.message
    )

    companion object {
        private fun codeToInt(code: Any?): Int =
            when (code) {
                is Number -> code.toInt()
                is Enum<*> -> code.ordinal
                else -> -1
            }
    }
}
