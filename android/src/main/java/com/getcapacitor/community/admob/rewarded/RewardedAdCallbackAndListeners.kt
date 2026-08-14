package com.getcapacitor.community.admob.rewarded

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdMobRevenueData
import com.getcapacitor.community.admob.models.AdOptions
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.gms.common.util.BiConsumer

object RewardedAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardAdPluginEvents.Rewarded, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>, adOptions: AdOptions): RewardedAdLoadCallback {
        return object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                ad.setImmersiveMode(immersiveMode ?: false)
                ad.fullScreenContentCallback = FullscreenPluginCallback(
                        RewardAdPluginEvents, notifyListenersFunction)

                ad.setOnPaidEventListener { adValue ->
                    val responseInfo = ad.responseInfo
                    val networkName = responseInfo?.mediationAdapterClassName ?: ""
                    val impressionId = responseInfo?.responseId ?: ""
                    val revenueData = AdMobRevenueData(adValue, ad.adUnitId, networkName, impressionId)
                    notifyListenersFunction.accept(RewardAdPluginEvents.AdImpression, revenueData)
                }

                if(adOptions.ssvInfo.hasInfo){
                    val ssvOptions = ServerSideVerificationOptions.Builder()
                    adOptions.ssvInfo.customData?.let {
                        ssvOptions.setCustomData(it)
                    }

                    adOptions.ssvInfo.userId?.let {
                        ssvOptions.setUserId(it)
                    }
                    ad.setServerSideVerificationOptions(ssvOptions.build())
                }

                AdRewardExecutor.preparedAds[ad.adUnitId] = ad
                AdRewardExecutor.lastPreparedAdId = ad.adUnitId

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardAdPluginEvents.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

}