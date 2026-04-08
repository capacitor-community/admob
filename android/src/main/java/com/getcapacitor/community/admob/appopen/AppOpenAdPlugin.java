package com.getcapacitor.community.admob.appopen;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.models.AdMobPluginError;

public class AppOpenAdPlugin {

    public interface EventNotifier {
        void notify(String eventName, JSObject data);
    }

    private AppOpenAdManager appOpenAdManager;

    private static void runOnMain(Activity activity, Runnable runnable) {
        if (activity != null) {
            activity.runOnUiThread(runnable);
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public void loadAppOpen(Context context, Activity activity, PluginCall call, EventNotifier notifier) {
        if (context == null) {
            call.reject("Context is not available");
            return;
        }

        String adUnitId = call.getString("adId");
        if (adUnitId == null) {
            call.reject("adId is required");
            return;
        }

        final Context appContext = context.getApplicationContext();
        runOnMain(activity, () -> {
            if (appOpenAdManager == null || !adUnitId.equals(appOpenAdManager.getAdUnitId())) {
                appOpenAdManager = new AppOpenAdManager(adUnitId);
            }

            appOpenAdManager.loadAd(
                appContext,
                () -> {
                    notifier.notify(AppOpenAdPluginEvents.Loaded, new JSObject());
                    call.resolve();
                },
                (loadAdError) -> {
                    String errorMessage = loadAdError != null ? loadAdError.getMessage() : "Failed to load App Open Ad";
                    int errorCode = loadAdError != null ? loadAdError.getCode() : -1;
                    notifier.notify(AppOpenAdPluginEvents.FailedToLoad, new AdMobPluginError(errorCode, errorMessage));
                    call.reject(errorMessage);
                }
            );
        });
    }

    public void showAppOpen(Activity activity, PluginCall call, EventNotifier notifier) {
        if (activity == null) {
            call.reject("Activity is not available");
            return;
        }

        activity.runOnUiThread(() -> {
            if (appOpenAdManager == null || !appOpenAdManager.isAdLoaded()) {
                call.reject("App Open Ad is not loaded");
                return;
            }

            appOpenAdManager.showAdIfAvailable(
                activity,
                () -> {
                    notifier.notify(AppOpenAdPluginEvents.Showed, new JSObject());
                },
                () -> {
                    notifier.notify(AppOpenAdPluginEvents.Dismissed, new JSObject());
                    call.resolve();
                },
                (adError) -> {
                    String errorMessage = adError != null ? adError.getMessage() : "Failed to show App Open Ad";
                    int errorCode = adError != null ? adError.getCode() : -1;
                    notifier.notify(AppOpenAdPluginEvents.FailedToShow, new AdMobPluginError(errorCode, errorMessage));
                    call.reject(errorMessage);
                }
            );
        });
    }

    public void isAppOpenLoaded(Activity activity, PluginCall call) {
        runOnMain(activity, () -> {
            boolean loaded = appOpenAdManager != null && appOpenAdManager.isAdLoaded();
            JSObject result = new JSObject();
            result.put("value", loaded);
            call.resolve(result);
        });
    }
}
