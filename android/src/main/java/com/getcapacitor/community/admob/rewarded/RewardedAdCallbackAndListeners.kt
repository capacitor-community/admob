package com.getcapacitor.community.admob.rewarded

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AbMobPluginError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.common.util.BiConsumer

object RewardedAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardAdPluginEvents.Rewarded.webEventName, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): RewardedAdLoadCallback {
        return object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                AdRewardExecutor.mRewardedAd = ad
                AdRewardExecutor.mRewardedAd.fullScreenContentCallback = getFullScreenContentCallback(notifyListenersFunction)

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardAdPluginEvents.Loaded.webEventName, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AbMobPluginError(adError)

                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad.webEventName, adMobError)
                call.reject(adError.message)
            }
        }
    }

    fun getFullScreenContentCallback(
            notifyListenersFunction: BiConsumer<String, JSObject>
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(RewardAdPluginEvents.Showed.webEventName, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                val adMobError = AbMobPluginError(adError)
                notifyListenersFunction.accept(
                        RewardAdPluginEvents.FailedToShow.webEventName, adMobError
                )
            }

            override fun onAdDismissedFullScreenContent() {
                notifyListenersFunction.accept(RewardAdPluginEvents.Dismissed.webEventName, JSObject())
            }
        }
    }
}