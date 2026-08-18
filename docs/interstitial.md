# Interstitial Ads

Interstitial ads are full-screen ads that cover the host app. Show them at a natural transition, such as between activities or game levels. The user can tap through or close the ad and return to the app. Google's interstitial guides for [Android](https://developers.google.com/admob/android/interstitial) and [iOS](https://developers.google.com/admob/ios/interstitial) explain the format.

Use an interstitial when the user should not receive an in-app reward. Call this after [initialize](./configuration.md) and [consent](./consent.md). Prepare the ad ahead of time, register listeners first, and show it only when it is ready.

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

Request fields are defined on [`AdOptions`](../README.md#adoptions). See [Testing](./testing.md) for `isTesting`.

Load, show, dismissal, and failure events are listed on [Ad Events](./events.md).
