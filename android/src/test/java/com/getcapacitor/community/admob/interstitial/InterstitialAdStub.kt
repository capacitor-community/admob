package com.getcapacitor.community.admob.interstitial

import android.app.Activity
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.interstitial.InterstitialAd

internal class InterstitialAdStub: InterstitialAd() {

     private var fullScreenContentCallback: FullScreenContentCallback? = null

    override fun getAdUnitId(): String {
        return "adUnit"
    }

    override fun show(p0: Activity) {
        TODO("Not yet implemented")
    }

    override fun setFullScreenContentCallback(p0: FullScreenContentCallback?) {
        fullScreenContentCallback = p0
    }

    override fun getFullScreenContentCallback(): FullScreenContentCallback? {
        return fullScreenContentCallback
    }

    override fun setImmersiveMode(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getResponseInfo(): ResponseInfo {
        TODO("Not yet implemented")
    }

    override fun setOnPaidEventListener(p0: OnPaidEventListener?) {
        TODO("Not yet implemented")
    }

    override fun getOnPaidEventListener(): OnPaidEventListener? {
        TODO("Not yet implemented")
    }

}