package com.getcapacitor.community.admob.appopen;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.FullScreenContentCallback;

public class AppOpenAdManager {
    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private String adUnitId;

    public AppOpenAdManager(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public void loadAd(Context context, final Runnable onLoaded, final Runnable onFailed) {
        if (isLoadingAd || appOpenAd != null) {
          return;
        }
        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    appOpenAd = ad;
                    isLoadingAd = false;
                    if (onLoaded != null) {
                        onLoaded.run();
                    }
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    isLoadingAd = false;
                    if (onFailed != null) {
                        onFailed.run();
                    }
                }
            }
        );
    }

    public void showAdIfAvailable(Activity activity, final Runnable onClosed, final Runnable onFailedToShow) {
        if (appOpenAd == null || isShowingAd) {
            if (onFailedToShow != null) onFailedToShow.run();
            return;
        }
        isShowingAd = true;
        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                if (onClosed != null) onClosed.run();
            }
            @Override
            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                appOpenAd = null;
                isShowingAd = false;
                if (onFailedToShow != null) onFailedToShow.run();
            }
        });
        appOpenAd.show(activity);
    }

    public boolean isAdLoaded() {
        return appOpenAd != null;
    }
}
