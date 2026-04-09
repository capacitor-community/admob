package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AdMobPluginError

class AppOpenAdPlugin {

    fun interface EventNotifier {
        fun notify(eventName: String, data: JSObject)
    }

    private var appOpenAdManager: AppOpenAdManager? = null

    private fun runOnMain(activity: Activity?, runnable: Runnable) {
        if (activity != null) {
            activity.runOnUiThread(runnable)
        } else {
            Handler(Looper.getMainLooper()).post(runnable)
        }
    }

    fun loadAppOpen(context: Context?, activity: Activity?, call: PluginCall, notifier: EventNotifier) {
        if (context == null) {
            call.reject("Context is not available")
            return
        }

        val adUnitId = call.getString("adId")
        if (adUnitId == null) {
            call.reject("adId is required")
            return
        }

        val appContext = context.applicationContext
        runOnMain(activity) {
            if (appOpenAdManager == null || adUnitId != appOpenAdManager?.adUnitId) {
                appOpenAdManager = AppOpenAdManager(adUnitId)
            }

            appOpenAdManager?.loadAd(
                appContext,
                onLoaded = {
                    val adInfo = JSObject().apply {
                        put("adUnitId", adUnitId)
                    }
                    notifier.notify(AppOpenAdPluginEvents.Loaded, adInfo)
                    call.resolve(adInfo)
                },
                onFailed = { loadAdError ->
                    val errorMessage = loadAdError?.message ?: "Failed to load App Open Ad"
                    val errorCode = loadAdError?.code ?: -1
                    notifier.notify(AppOpenAdPluginEvents.FailedToLoad, AdMobPluginError(errorCode, errorMessage))
                    call.reject(errorMessage)
                }
            )
        }
    }

    fun showAppOpen(activity: Activity?, call: PluginCall, notifier: EventNotifier) {
        if (activity == null) {
            call.reject("Activity is not available")
            return
        }

        runOnMain(activity) {
            if (appOpenAdManager == null || appOpenAdManager?.isAdLoaded != true) {
                call.reject("App Open Ad is not loaded")
                return@runOnMain
            }

            appOpenAdManager?.showAdIfAvailable(
                activity,
                onOpened = {
                    notifier.notify(AppOpenAdPluginEvents.Opened, JSObject())
                },
                onClosed = {
                    notifier.notify(AppOpenAdPluginEvents.Closed, JSObject())
                    call.resolve()
                },
                onFailedToShow = { adError ->
                    val errorMessage = adError?.message ?: "Failed to show App Open Ad"
                    val errorCode = adError?.code ?: -1
                    notifier.notify(AppOpenAdPluginEvents.FailedToShow, AdMobPluginError(errorCode, errorMessage))
                    call.reject(errorMessage)
                }
            )
        }
    }

    fun isAppOpenLoaded(activity: Activity?, call: PluginCall) {
        runOnMain(activity) {
            val loaded = appOpenAdManager?.isAdLoaded ?: false
            val result = JSObject().apply {
                put("value", loaded)
            }
            call.resolve(result)
        }
    }
}
