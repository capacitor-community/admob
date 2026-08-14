package com.getcapacitor.community.admob.interstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdMobRevenueData
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.common.util.BiConsumer

object InterstitialAdCallbackAndListeners {

    fun getInterstitialAdLoadCallback(call: PluginCall,
                                      notifyListenersFunction: BiConsumer<String, JSObject>,
    ): InterstitialAdLoadCallback {
        return object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                ad.fullScreenContentCallback = FullscreenPluginCallback(InterstitialAdPluginPluginEvent, notifyListenersFunction)
                ad.setImmersiveMode(immersiveMode ?: false)

                ad.setOnPaidEventListener { adValue ->
                    val responseInfo = ad.responseInfo
                    val networkName = responseInfo?.mediationAdapterClassName ?: ""
                    val impressionId = responseInfo?.responseId ?: ""
                    val revenueData = AdMobRevenueData(adValue, ad.adUnitId, networkName, impressionId)
                    notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.AdImpression, revenueData)
                }

                AdInterstitialExecutor.preparedAds[ad.adUnitId] = ad
                AdInterstitialExecutor.lastPreparedAdId = ad.adUnitId

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
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
}
