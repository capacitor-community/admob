package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

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
        val request = AdRequest.Builder(adUnitId).build()

        AppOpenAd.load(
            request,
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    onLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoadingAd = false
                    onFailed(adError)
                }
            }
        )
    }

    fun showAdIfAvailable(
        activity: Activity,
        onOpened: () -> Unit,
        onClosed: () -> Unit,
        onFailedToShow: (FullScreenContentError?) -> Unit
    ) {
        if (appOpenAd == null || isShowingAd) {
            onFailedToShow(null)
            return
        }

        isShowingAd = true
        appOpenAd?.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                onOpened()
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                appOpenAd = null
                isShowingAd = false
                onFailedToShow(fullScreenContentError)
            }
        }

        appOpenAd?.show(activity)
    }
}
