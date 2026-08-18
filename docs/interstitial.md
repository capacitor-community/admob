---
title: Interstitial Ads
---

# Interstitial Ads

## Loading and showing an interstitial

Use an Interstitial ad at a natural break when the user should not receive an in-app reward.

```ts
import { AdMob, AdOptions, AdLoadInfo, AdMobRevenueData, InterstitialAdPluginEvents } from '@capacitor-community/admob';

export async function interstitial(): Promise<void> {
  AdMob.addListener(InterstitialAdPluginEvents.Loaded, (info: AdLoadInfo) => {
    // Subscribe prepared interstitial
  });

  AdMob.addListener(InterstitialAdPluginEvents.AdImpression, (data: AdMobRevenueData) => {
    // Forward impression-level revenue to your analytics provider.
    console.log(data);
  });

  const options: AdOptions = {
    adId: 'YOUR ADID',
    // isTesting: true
    // npa: true
    // immersiveMode: true
  };
  await AdMob.prepareInterstitial(options);
  await AdMob.showInterstitial();

  // You can also prepare multiple interstitials and show a specific one by passing its adId:
  await AdMob.prepareInterstitial({ adId: 'ca-app-pub-xxx/interstitial-1' });
  await AdMob.prepareInterstitial({ adId: 'ca-app-pub-xxx/interstitial-2' });

  // Show a specific prepared ad
  await AdMob.showInterstitial({ adId: 'ca-app-pub-xxx/interstitial-1' });

  // Or omit adId to show the most recently prepared one (default behavior)
  await AdMob.showInterstitial();
}
```
