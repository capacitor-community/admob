package com.getcapacitor.community.admob.callbackandlisteners

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.executors.AdRewardExecutor
import com.getcapacitor.community.admob.helpers.AdViewIdHelper
import com.getcapacitor.community.admob.models.FullScreenAdEventName
import com.getcapacitor.community.admob.models.RewardAdPluginEvents
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
                AdRewardExecutor.mRewardedAd.fullScreenContentCallback = AdViewIdHelper.getFullScreenContentCallback(notifyListenersFunction)
                call.resolve()
                notifyListenersFunction.accept(FullScreenAdEventName.onAdLoaded.name, JSObject())
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = JSObject()
                adMobError.put("code", adError.code)
                adMobError.put("reason", adError.message)

                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad.webEventName, adMobError)
            }
        }
    }
}