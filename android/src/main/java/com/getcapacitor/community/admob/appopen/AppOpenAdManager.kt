package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(val adUnitId: String) {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    val isAdLoaded: Boolean
        get() = appOpenAd != null

    fun loadAd(context: Context, onLoaded: () -> Unit, onFailed: (LoadAdError?) -> Unit) {
        if (appOpenAd != null) {
            onLoaded()
            return
        }

        if (isLoadingAd) {
            onFailed(null)
            return
        }

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
                    isLoadingAd = false
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
        if (appOpenAd == null || isShowingAd) {
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
                isShowingAd = false
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                onFailedToShow(adError)
            }
        }

        appOpenAd?.show(activity)
    }
}
