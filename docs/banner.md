---
title: Banner Ads
---

# Banner Ads

Banner ads are rectangular ads that occupy a portion of an app's layout. They can stay on screen while the user interacts with the app, typically anchored at the top or bottom. Google's banner guides for [Android](https://developers.google.com/admob/android/banner) and [iOS](https://developers.google.com/admob/ios/banner) explain the format.

This plugin draws the banner on the native screen (above the web view). Register listeners before calling `showBanner` so the first load and size events are not missed.

```ts
import {
  AdMob,
  AdMobBannerSize,
  AdMobRevenueData,
  BannerAdOptions,
  BannerAdPluginEvents,
  BannerAdPosition,
  BannerAdSize,
} from '@capacitor-community/admob';

const handles = await Promise.all([
  AdMob.addListener(BannerAdPluginEvents.Loaded, () => {
    console.log('Banner loaded');
  }),
  AdMob.addListener(BannerAdPluginEvents.SizeChanged, (size: AdMobBannerSize) => {
    console.log('Banner size', size.width, size.height);
  }),
  AdMob.addListener(BannerAdPluginEvents.FailedToLoad, (error) => {
    console.error(error);
  }),
  AdMob.addListener(BannerAdPluginEvents.AdPaid, (data: AdMobRevenueData) => {
    // Forward impression-level revenue to your analytics provider.
    console.log(data);
  }),
]);

const options: BannerAdOptions = {
  adId: 'YOUR_AD_UNIT_ID',
  adSize: BannerAdSize.ADAPTIVE_BANNER,
  position: BannerAdPosition.BOTTOM_CENTER,
  margin: 0,
  // isTesting: true,
  // npa: true,
};
await AdMob.showBanner(options);
```

Use `SizeChanged` to reserve layout space so the banner does not cover app content. A hidden, removed, or failed banner can report both dimensions as `0`.

## Ionic: keep content above the banner

The banner is a native overlay above the WebView. Ionic does not shrink `ion-content` for you. The [Angular demo](../demo/angular) listens to `SizeChanged` and sets margin on `ion-router-outlet` ([`BannerViewportService`](../demo/angular/src/app/shared/banner-viewport.service.ts)):

```ts
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';

const outlet = document.querySelector<HTMLElement>('ion-router-outlet');

await AdMob.addListener(BannerAdPluginEvents.SizeChanged, (size) => {
  if (!outlet) {
    return;
  }
  outlet.style.marginTop = '';
  outlet.style.marginBottom = '';
  if (size.height === 0) {
    return;
  }
  const safeAreaBottom = window.getComputedStyle(document.body).getPropertyValue('--ion-safe-area-bottom');
  outlet.style.marginBottom = `calc(${safeAreaBottom} + ${size.height}px)`;
});
```

That snippet matches the demo's bottom banner. For `TOP_CENTER`, the demo sets `marginTop` to `${size.height}px` instead. `hideBanner` / `removeBanner` clear the margin; `resumeBanner` restores it.

Request fields are defined on [`BannerAdOptions`](../README.md#banneradoptions). See [Testing](./testing.md) for `isTesting`.

## Lifecycle

- `hideBanner()` temporarily hides the current banner.
- `resumeBanner()` shows a hidden banner again.
- `removeBanner()` destroys it. Call `showBanner()` to create another one.

Release listener handles when the owning screen is destroyed:

```ts
for (const handle of handles) {
  await handle.remove();
}
await AdMob.removeBanner();
```

Banner impression-level revenue is emitted on `BannerAdPluginEvents.AdPaid`. Full-screen formats emit the same `AdMobRevenueData` through their `AdImpression` event. See [Ad Events](./events.md).
