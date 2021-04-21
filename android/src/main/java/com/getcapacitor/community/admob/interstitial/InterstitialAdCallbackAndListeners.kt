package com.getcapacitor.community.admob.interstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AbMobPluginError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.common.util.BiConsumer

object InterstitialAdCallbackAndListeners {


    fun getInterstitialAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): InterstitialAdLoadCallback {
        return object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                AdInterstitialExecutor.interstitialAd = ad
                AdInterstitialExecutor.interstitialAd.fullScreenContentCallback = getFullScreenContentCallback(notifyListenersFunction)

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(InterstitialAdPluginEvents.Loaded.webEventName, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AbMobPluginError(adError)

                notifyListenersFunction.accept(InterstitialAdPluginEvents.FailedToLoad.webEventName, adMobError)
                call.reject(adError.message)
            }
        }
    }

    fun getFullScreenContentCallback(
            notifyListenersFunction: BiConsumer<String, JSObject>
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(InterstitialAdPluginEvents.Showed.webEventName, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val adMobError = AbMobPluginError(adError)
                notifyListenersFunction.accept(
                        InterstitialAdPluginEvents.FailedToShow.webEventName, adMobError
                )
            }

            override fun onAdDismissedFullScreenContent() {
                notifyListenersFunction.accept(InterstitialAdPluginEvents.Dismissed.webEventName, JSObject())
            }
        }
    }
}