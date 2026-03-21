package com.getcapacitor.community.admob.appopen;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenAdManager {

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private String adUnitId;

    public AppOpenAdManager(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public void loadAd(Context context, final Runnable onLoaded, final Runnable onFailed) {
        if (appOpenAd != null) {
            if (onLoaded != null) {
                onLoaded.run();
            }
            return;
        }

        if (isLoadingAd) {
            if (onFailed != null) {
                onFailed.run();
            }
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();

        // play-services-ads 24.x: orientation overload removed; SDK picks orientation from the activity.
        AppOpenAd.load(
            context,
            adUnitId,
            request,
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

    public void showAdIfAvailable(Activity activity, final Runnable onOpened, final Runnable onClosed, final Runnable onFailedToShow) {
        if (appOpenAd == null || isShowingAd) {
            if (onFailedToShow != null) {
                onFailedToShow.run();
            }
            return;
        }

        isShowingAd = true;
        appOpenAd.setFullScreenContentCallback(
            new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    if (onOpened != null) {
                        onOpened.run();
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    isShowingAd = false;

                    if (onClosed != null) {
                        onClosed.run();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                    appOpenAd = null;
                    isShowingAd = false;

                    if (onFailedToShow != null) {
                        onFailedToShow.run();
                    }
                }
            }
        );

        appOpenAd.show(activity);
    }

    public boolean isAdLoaded() {
        return appOpenAd != null;
    }
}
