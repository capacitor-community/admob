package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.getcapacitor.community.admob.helpers.AdExpiration
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

class AppOpenAdManager(val adUnitId: String) {

    private var appOpenAd: AppOpenAd? = null
    private var expiration: AdExpiration? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var onPaidEvent: ((AdValue, String, String) -> Unit)? = null

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
        this.onPaidEvent = onPaidEvent
        isLoadingAd = true
        val request = AdRequest.Builder(adUnitId).build()

        AppOpenAd.load(
            request,
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    expiration = AdExpiration(4 * 3600)
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
        if (!isAdLoaded || isShowingAd) {
            onFailedToShow(null)
            return
        }

        isShowingAd = true
        appOpenAd?.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdPaid(value: AdValue) {
                val info = appOpenAd?.getResponseInfo()
                onPaidEvent?.invoke(value, info?.adapterClassName ?: "", info?.responseId ?: "")
            }
            override fun onAdShowedFullScreenContent() {
                onOpened()
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                expiration = null
                isShowingAd = false
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                appOpenAd = null
                expiration = null
                isShowingAd = false
                onFailedToShow(fullScreenContentError)
            }
        }

        appOpenAd?.show(activity)
    }
}
