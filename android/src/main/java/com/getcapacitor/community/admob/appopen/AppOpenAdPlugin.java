package com.getcapacitor.community.admob.appopen;

import android.app.Activity;
import android.content.Context;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.community.admob.AdMobPlugin;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "AppOpenAd")
public class AppOpenAdPlugin extends AdMobPlugin {
    private AppOpenAdManager appOpenAdManager;

    @PluginMethod
    public void loadAppOpen(PluginCall call) {
        String adUnitId = call.getString("adUnitId");
        if (adUnitId == null) {
            call.reject("adUnitId is required");
            return;
        }
        if (appOpenAdManager == null) {
            appOpenAdManager = new AppOpenAdManager(adUnitId);
        }
        Context context = getContext();
        appOpenAdManager.loadAd(context, () -> {
            notifyListeners("appOpenAdLoaded", new JSONObject());
            call.resolve();
        }, () -> {
            notifyListeners("appOpenAdFailedToLoad", new JSONObject());
            call.reject("Failed to load App Open Ad");
        });
    }

    @PluginMethod
    public void showAppOpen(PluginCall call) {
        Activity activity = getActivity();
        appOpenAdManager.showAdIfAvailable(activity, () -> {
            notifyListeners("appOpenAdClosed", new JSONObject());
            call.resolve();
        }, () -> {
            notifyListeners("appOpenAdFailedToShow", new JSONObject());
            call.reject("Failed to show App Open Ad");
        });
    }

    @PluginMethod
    public void isAppOpenLoaded(PluginCall call) {
        boolean loaded = appOpenAdManager != null && appOpenAdManager.isAdLoaded();
        try {
            JSONObject result = new JSONObject();
            result.put("value", loaded);
            call.resolve(result);
        } catch (JSONException e) {
            call.reject("JSON error");
        }
    }
}
