package com.getcapacitor.community.admob.appopen;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

public class AppOpenAdPlugin {

    public interface EventNotifier {
        void notify(String eventName, JSObject data);
    }

    private AppOpenAdManager appOpenAdManager;

    public void loadAppOpen(Context context, PluginCall call, EventNotifier notifier) {
        if (context == null) {
            call.reject("Context is not available");
            return;
        }

        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null) {
            call.reject("adUnitId is required");
            return;
        }
        if (appOpenAdManager == null) {
            appOpenAdManager = new AppOpenAdManager(adUnitId);
        }

        appOpenAdManager.loadAd(
            context,
            () -> {
                notifier.notify("appOpenAdLoaded", new JSObject());
                call.resolve();
            },
            () -> {
                notifier.notify("appOpenAdFailedToLoad", new JSObject());
                call.reject("Failed to load App Open Ad");
            }
        );
    }

    public void showAppOpen(Activity activity, PluginCall call, EventNotifier notifier) {
        if (activity == null) {
            call.reject("Activity is not available");
            return;
        }

        if (appOpenAdManager == null || !appOpenAdManager.isAdLoaded()) {
            call.reject("App Open Ad is not loaded");
            return;
        }

        appOpenAdManager.showAdIfAvailable(
            activity,
            () -> {
                notifier.notify("appOpenAdOpened", new JSObject());
            },
            () -> {
                notifier.notify("appOpenAdClosed", new JSObject());
                call.resolve();
            },
            () -> {
                notifier.notify("appOpenAdFailedToShow", new JSObject());
                call.reject("Failed to show App Open Ad");
            }
        );
    }

    public void isAppOpenLoaded(PluginCall call) {
        boolean loaded = appOpenAdManager != null && appOpenAdManager.isAdLoaded();
        JSObject result = new JSObject();
        result.put("value", loaded);
        call.resolve(result);
    }
}
