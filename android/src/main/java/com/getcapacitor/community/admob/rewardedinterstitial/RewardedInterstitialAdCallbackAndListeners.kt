package com.getcapacitor.community.admob.rewardedinterstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdOptions
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.common.util.BiConsumer

object RewardedInterstitialAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Rewarded, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>, adOptions: AdOptions): RewardedInterstitialAdLoadCallback {
        return object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                AdRewardInterstitialExecutor.mRewardedInterstitialAd = ad
                AdRewardInterstitialExecutor.mRewardedInterstitialAd.fullScreenContentCallback = FullscreenPluginCallback(
                        RewardInterstitialAdPluginEvents, notifyListenersFunction)

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

}