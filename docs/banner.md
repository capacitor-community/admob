---
title: Banner Ads
---

# Banner Ads

Banner ads occupy part of the native view. Register listeners before calling `showBanner` so the first load and size events are not missed.

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

## Options

| Option      | Description                                                                                          |
| ----------- | ---------------------------------------------------------------------------------------------------- |
| `adId`      | Banner ad unit ID.                                                                                   |
| `adSize`    | Banner size. Prefer `ADAPTIVE_BANNER` for new integrations. Defaults to `ADAPTIVE_BANNER`.           |
| `position`  | `TOP_CENTER`, `CENTER`, or `BOTTOM_CENTER`. Defaults to `TOP_CENTER`.                                |
| `margin`    | Margin in logical units (dp / points). Bottom margin for `BOTTOM_CENTER`, top margin for `TOP_CENTER`. |
| `isTesting` | Request a Google test ad. See [Testing](./testing.md).                                               |
| `npa`       | Request a non-personalized ad.                                                                       |

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
