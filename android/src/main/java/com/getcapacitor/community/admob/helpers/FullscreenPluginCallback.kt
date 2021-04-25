package com.getcapacitor.community.admob.helpers

import com.getcapacitor.JSObject
import com.getcapacitor.community.admob.models.AbMobPluginError
import com.getcapacitor.community.admob.models.LoadPluginEventNames
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.common.util.BiConsumer

class FullscreenPluginCallbackCreator(private val loadPluginObject: LoadPluginEventNames,
                                      private val notifyListenersFunction: BiConsumer<String, JSObject>) {

    fun create(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(loadPluginObject.Showed, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val adMobError = AbMobPluginError(adError)
                notifyListenersFunction.accept(
                        loadPluginObject.FailedToShow, adMobError
                )
            }

            override fun onAdDismissedFullScreenContent() {
                notifyListenersFunction.accept(loadPluginObject.Dismissed, JSObject())
            }
        }
    }
}