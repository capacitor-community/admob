---
title: Interstitial Ads
---

# Interstitial Ads

Use an interstitial at a natural break when the user should not receive an in-app reward. Prepare the ad ahead of time, register listeners first, and show it only when it is ready.

```ts
import {
  AdLoadInfo,
  AdMob,
  AdMobRevenueData,
  AdOptions,
  InterstitialAdPluginEvents,
} from '@capacitor-community/admob';

await AdMob.addListener(InterstitialAdPluginEvents.Loaded, (info: AdLoadInfo) => {
  console.log('Interstitial loaded', info.adUnitId);
});
await AdMob.addListener(InterstitialAdPluginEvents.FailedToLoad, console.error);
await AdMob.addListener(InterstitialAdPluginEvents.AdImpression, (data: AdMobRevenueData) => {
  console.log(data);
});

const options: AdOptions = {
  adId: 'YOUR_AD_UNIT_ID',
  // isTesting: true,
  // npa: true,
  // immersiveMode: true,
};
const { adUnitId } = await AdMob.prepareInterstitial(options);
await AdMob.showInterstitial({ adId: adUnitId });
```

When no `adId` is passed to `showInterstitial()`, the most recently prepared ad is shown.

## Prepare more than one ad

```ts
await AdMob.prepareInterstitial({ adId: 'ca-app-pub-xxx/interstitial-1' });
await AdMob.prepareInterstitial({ adId: 'ca-app-pub-xxx/interstitial-2' });

await AdMob.showInterstitial({ adId: 'ca-app-pub-xxx/interstitial-1' });
```

## Options

| Option          | Description                                                                 |
| --------------- | --------------------------------------------------------------------------- |
| `adId`          | Interstitial ad unit ID.                                                    |
| `isTesting`     | Request a Google test ad. See [Testing](./testing.md).                      |
| `npa`           | Request a non-personalized ad.                                              |
| `immersiveMode` | Android only. Present the full-screen ad in immersive mode.                 |

Load, show, dismissal, and failure events are listed on [Ad Events](./events.md).
