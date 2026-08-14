package com.getcapacitor.community.admob.helpers

import com.getcapacitor.JSObject
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.LoadPluginEventNames
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.common.util.BiConsumer

class FullscreenPluginCallback(
    private val loadPluginObject: LoadPluginEventNames,
    private val notifyListenersFunction: BiConsumer<String, JSObject>,
    private val onCompleted: Runnable? = null
): FullScreenContentCallback() {

    override fun onAdShowedFullScreenContent() {
        notifyListenersFunction.accept(loadPluginObject.Showed, JSObject())
    }

    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        onCompleted?.run()
        val adMobError = AdMobPluginError(adError)
        notifyListenersFunction.accept(
                loadPluginObject.FailedToShow, adMobError
        )
    }

    override fun onAdDismissedFullScreenContent() {
        onCompleted?.run()
        notifyListenersFunction.accept(loadPluginObject.Dismissed, JSObject())
    }
}