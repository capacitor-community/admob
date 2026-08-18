package com.getcapacitor.community.admob.interstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdMobRevenueData
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import java.util.function.BiConsumer

object InterstitialAdCallbackAndListeners {

    fun getInterstitialAdLoadCallback(call: PluginCall,
                                      notifyListenersFunction: BiConsumer<String, JSObject>,
                                      adUnitId: String,
    ): AdLoadCallback<InterstitialAd> {
        return object : AdLoadCallback<InterstitialAd> {
            override fun onAdLoaded(ad: InterstitialAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                ad.setImmersiveMode(immersiveMode ?: false)
                ad.adEventCallback =
                    object : InterstitialAdEventCallback {
                        override fun onAdPaid(value: com.google.android.libraries.ads.mobile.sdk.common.AdValue) {
                            val revenueData = AdMobRevenueData(value, adUnitId, "", "")
                            notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.AdImpression, revenueData)
                        }
                    }

                AdInterstitialExecutor.preparedAds[adUnitId] = ad
                AdInterstitialExecutor.lastPreparedAdId = adUnitId

                val adInfo = JSObject()
                adInfo.put("adUnitId", adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

    fun getInterstitialAdEventCallback(
        notifyListenersFunction: BiConsumer<String, JSObject>,
        onCompleted: Runnable? = null,
    ): InterstitialAdEventCallback {
        return object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.Showed, JSObject())
            }

            override fun onAdDismissedFullScreenContent() {
                onCompleted?.run()
                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.Dismissed, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                onCompleted?.run()
                notifyListenersFunction.accept(
                    InterstitialAdPluginPluginEvent.FailedToShow,
                    AdMobPluginError(fullScreenContentError),
                )
            }
        }
    }
}
