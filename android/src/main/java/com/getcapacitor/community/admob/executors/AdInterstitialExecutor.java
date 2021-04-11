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
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.gms.common.util.BiConsumer;

public class AdInterstitialExecutor extends Executor {

    public static RewardedInterstitialAd mRewardedAd;

    public AdInterstitialExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdRewardExecutor");
    }

    public void prepareInterstitial(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        final AdOptions adOptions = AdOptions.getFactory().createRewardVideoOptions(call);

        try {
            activitySupplier
                .get()
                .runOnUiThread(
                    () -> {
                        final AdRequest adRequest = RequestHelper.createRequest(adOptions);
                        final String id = AdViewIdHelper.getFinalAdId(adOptions, adRequest, logTag, contextSupplier.get());
                        RewardedInterstitialAd.load(
                            activitySupplier.get(),
                            id,
                            adRequest,
                            getRewardedInterstitialAdLoadCallbackk(call, notifyListenersFunction)
                        );
                    }
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }


    public void showInterstitial(final PluginCall call) {
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
     * Will return a {@link RewardedInterstitialAdLoadCallback} ready to attach to a new created
     * {@link RewardedInterstitialAd}
     */
    static RewardedInterstitialAdLoadCallback getRewardedInterstitialAdLoadCallbackk(
        PluginCall call,
        BiConsumer<String, JSObject> notifyListenersFunction
    ) {
        return new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedInterstitialAd ad) {
                mRewardedAd = ad;
                mRewardedAd.setFullScreenContentCallback(AdViewIdHelper.getFullScreenContentCallback(notifyListenersFunction));
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
     * {@link RewardedInterstitialAd}
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
