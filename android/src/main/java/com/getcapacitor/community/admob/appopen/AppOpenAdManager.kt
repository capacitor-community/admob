package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import com.getcapacitor.community.admob.helpers.AdExpiration
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(val adUnitId: String) {

    private var appOpenAd: AppOpenAd? = null
    private var expiration: AdExpiration? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    val isAdLoaded: Boolean
        get() = appOpenAd != null && expiration?.isExpired() == false

    fun loadAd(context: Context, onLoaded: () -> Unit, onFailed: (LoadAdError?) -> Unit, onPaidEvent: (AdValue, String, String) -> Unit) {
        if (isAdLoaded) {
            onLoaded()
            return
        }

        if (isShowingAd) {
            onFailed(null)
            return
        }

        if (isLoadingAd) {
            onFailed(null)
            return
        }

        appOpenAd = null
        expiration = null
        isLoadingAd = true
        val request = AdRequest.Builder().build()

        // play-services-ads 24.x: orientation overload removed; SDK picks orientation from the activity.
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    expiration = AdExpiration(4 * 3600)
                    isLoadingAd = false
                    ad.setOnPaidEventListener { adValue ->
                        val networkName = ad.responseInfo?.mediationAdapterClassName ?: ""
                        val impressionId = ad.responseInfo?.responseId ?: ""
                        onPaidEvent(adValue, networkName, impressionId)
                    }
                    onLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    onFailed(loadAdError)
                }
            }
        )
    }

    fun showAdIfAvailable(
        activity: Activity,
        onOpened: () -> Unit,
        onClosed: () -> Unit,
        onFailedToShow: (AdError?) -> Unit
    ) {
        if (!isAdLoaded || isShowingAd) {
            onFailedToShow(null)
            return
        }

        isShowingAd = true
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onOpened()
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                expiration = null
                isShowingAd = false
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                expiration = null
                isShowingAd = false
                onFailedToShow(adError)
            }
        }

        appOpenAd?.show(activity)
    }
}
