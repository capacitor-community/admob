package com.getcapacitor.community.admob.rewarded

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdMobRevenueData
import com.getcapacitor.community.admob.models.AdOptions
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewarded.ServerSideVerificationOptions
import java.util.function.BiConsumer

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

    fun getRewardedAdLoadCallback(
        call: PluginCall,
        notifyListenersFunction: BiConsumer<String, JSObject>,
        adOptions: AdOptions,
        adUnitId: String,
    ): AdLoadCallback<RewardedAd> {
        return object : AdLoadCallback<RewardedAd> {
            override fun onAdLoaded(ad: RewardedAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                ad.setImmersiveMode(immersiveMode ?: false)

                if(adOptions.ssvInfo.hasInfo){
                    ad.setServerSideVerificationOptions(
                        ServerSideVerificationOptions(
                            adOptions.ssvInfo.userId ?: "",
                            adOptions.ssvInfo.customData ?: ""
                        )
                    )
                }

                ad.adEventCallback =
                    object : RewardedAdEventCallback {
                        override fun onAdPaid(value: AdValue) {
                            val revenueData = AdMobRevenueData(value, adUnitId, "", "")
                            notifyListenersFunction.accept(RewardAdPluginEvents.AdImpression, revenueData)
                        }
                    }

                AdRewardExecutor.preparedAds[adUnitId] = ad
                AdRewardExecutor.lastPreparedAdId = adUnitId

                val adInfo = JSObject()
                adInfo.put("adUnitId", adUnitId)
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

    fun getRewardedAdEventCallback(
        notifyListenersFunction: BiConsumer<String, JSObject>,
        onCompleted: Runnable? = null,
    ): RewardedAdEventCallback {
        return object : RewardedAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(RewardAdPluginEvents.Showed, JSObject())
            }

            override fun onAdDismissedFullScreenContent() {
                onCompleted?.run()
                notifyListenersFunction.accept(RewardAdPluginEvents.Dismissed, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                onCompleted?.run()
                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToShow, AdMobPluginError(fullScreenContentError))
            }
        }
    }

}