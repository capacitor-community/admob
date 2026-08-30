package com.getcapacitor.community.admob.rewardedinterstitial

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
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback
import java.util.function.BiConsumer

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

    fun getRewardedAdLoadCallback(
        call: PluginCall,
        notifyListenersFunction: BiConsumer<String, JSObject>,
        adOptions: AdOptions,
        adUnitId: String,
    ): AdLoadCallback<RewardedInterstitialAd> {
        return object : AdLoadCallback<RewardedInterstitialAd> {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                ad.adEventCallback =
                    object : RewardedInterstitialAdEventCallback {
                        override fun onAdPaid(value: AdValue) {
                            val revenueData = AdMobRevenueData(value, adUnitId, "", "")
                            notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.AdImpression, revenueData)
                        }
                    }

                AdRewardInterstitialExecutor.preparedAds[adUnitId] = ad
                AdRewardInterstitialExecutor.lastPreparedAdId = adUnitId

                val adInfo = JSObject()
                adInfo.put("adUnitId", adUnitId)
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

    fun getRewardedInterstitialAdEventCallback(
        notifyListenersFunction: BiConsumer<String, JSObject>,
        onCompleted: Runnable? = null,
    ): RewardedInterstitialAdEventCallback {
        return object : RewardedInterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Showed, JSObject())
            }

            override fun onAdDismissedFullScreenContent() {
                onCompleted?.run()
                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Dismissed, JSObject())
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                onCompleted?.run()
                notifyListenersFunction.accept(
                    RewardInterstitialAdPluginEvents.FailedToShow,
                    AdMobPluginError(fullScreenContentError),
                )
            }
        }
    }

}
