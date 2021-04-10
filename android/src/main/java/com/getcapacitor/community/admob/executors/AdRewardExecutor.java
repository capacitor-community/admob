package com.getcapacitor.community.admob.executors;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.FullScreenAdEventName;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.common.util.BiConsumer;

public class AdRewardExecutor extends Executor {

    public static RewardedAd mRewardedAd;

    public AdRewardExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    @PluginMethod
    public void prepareRewardVideoAd(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions adOptions = AdOptions.getFactory().createRewardVideoOptions(call);

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                        final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                        RewardedAd.load(contextSupplier.get(), id, adRequest, getRewardedAdLoadCallback(call, notifyListenersFunction));
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void showRewardVideoAd(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        mRewardedAd.show(activitySupplier.get(), getOnUserEarnedRewardListener(call, notifyListenersFunction));
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Will return a {@link RewardedAdLoadCallback} ready to attach to a new created
     * {@link RewardedAd}
     */
    static RewardedAdLoadCallback getRewardedAdLoadCallback(PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                mRewardedAd = ad;
                mRewardedAd.setFullScreenContentCallback(AdViewIdHelper.getFullScreenContentCallback(notifyListenersFunction));
                call.resolve();
                notifyListenersFunction.accept(FullScreenAdEventName.onAdLoaded.name(), new JSObject());
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                JSObject adMobError = new JSObject();
                adMobError.put("code", adError.getCode());
                adMobError.put("reason", adError.getMessage());

                notifyListenersFunction.accept(FullScreenAdEventName.onAdFailedToLoad.name(), new JSObject());
            }
        };
    }

    /**
     * Will return a {@link OnUserEarnedRewardListener} ready to attach to a new created
     * {@link RewardedAd}
     */
    static OnUserEarnedRewardListener getOnUserEarnedRewardListener(PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        return new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem item) {
                call.resolve(new JSObject().put("type", item.getType()).put("amount", item.getAmount()));
            }
        };
    }
}
