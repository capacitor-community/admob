package com.getcapacitor.community.admob.appopen

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdMobRevenueData

class AppOpenAdPlugin {

    fun interface EventNotifier {
        fun notify(eventName: String, data: JSObject)
    }

    private val preparedManagers = LinkedHashMap<String, AppOpenAdManager>()
    private var lastPreparedAdId: String? = null

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
            val manager = preparedManagers.getOrPut(adUnitId) { AppOpenAdManager(adUnitId) }

            manager.loadAd(
                appContext,
                onLoaded = {
                    lastPreparedAdId = adUnitId
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
                },
                onPaidEvent = { adValue, networkName, impressionId ->
                    val revenueData = AdMobRevenueData(adValue, adUnitId, networkName, impressionId)
                    notifier.notify(AppOpenAdPluginEvents.AdImpression, revenueData)
                }
            )
        }
    }

    fun showAppOpen(activity: Activity?, call: PluginCall, notifier: EventNotifier) {
        if (activity == null) {
            call.reject("Activity is not available")
            return
        }

        val adId = call.getString("adId") ?: lastPreparedAdId

        runOnMain(activity) {
            val manager = if (adId != null) preparedManagers[adId] else null

            if (manager == null || !manager.isAdLoaded) {
                call.reject("App Open Ad is not loaded")
                return@runOnMain
            }

            manager.showAdIfAvailable(
                activity,
                onOpened = {
                    notifier.notify(AppOpenAdPluginEvents.Opened, JSObject())
                },
                onClosed = {
                    preparedManagers.remove(adId)
                    if (lastPreparedAdId == adId) {
                        lastPreparedAdId = preparedManagers.entries.lastOrNull { it.value.isAdLoaded }?.key
                    }
                    notifier.notify(AppOpenAdPluginEvents.Closed, JSObject())
                    call.resolve()
                },
                onFailedToShow = { adError ->
                    preparedManagers.remove(adId)
                    if (lastPreparedAdId == adId) {
                        lastPreparedAdId = preparedManagers.entries.lastOrNull { it.value.isAdLoaded }?.key
                    }
                    val errorMessage = adError?.message ?: "Failed to show App Open Ad"
                    val errorCode = adError?.code ?: -1
                    notifier.notify(AppOpenAdPluginEvents.FailedToShow, AdMobPluginError(errorCode, errorMessage))
                    call.reject(errorMessage)
                }
            )
        }
    }

    fun isAppOpenLoaded(activity: Activity?, call: PluginCall) {
        val adId = call.getString("adId") ?: lastPreparedAdId
        runOnMain(activity) {
            val loaded = adId?.let { preparedManagers[it]?.isAdLoaded } ?: false
            val result = JSObject().apply {
                put("value", loaded)
            }
            call.resolve(result)
        }
    }
}
