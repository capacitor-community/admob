# Ad Events

Google documents these lifecycle callbacks on each format page, for example [banner ad events](https://developers.google.com/admob/android/banner#ad_events). This plugin exposes the same moments under the names in the table below, not the native `AdListener` method names. For the paid payload, see [impression-level ad revenue](https://developers.google.com/admob/android/impression-level-ad-revenue) ([iOS](https://developers.google.com/admob/ios/impression-level-ad-revenue)).

Register listeners before loading or showing an ad so that the first lifecycle and impression events are not missed.

## Adding and removing listeners

`AdMob.addListener` returns a handle. Await the registration, then call `remove()` when the owning screen is destroyed:

```ts
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';

const handle = await AdMob.addListener(BannerAdPluginEvents.Loaded, () => {
  console.log('Banner loaded');
});

await handle.remove();
```

## Common lifecycle events

| Event                     | Emitted when                                           |
| ------------------------- | ------------------------------------------------------ |
| `Loaded`                  | The ad finished loading and is ready to show.          |
| `FailedToLoad`            | The ad could not load. Check `AdMobError` for details. |
| `Showed` / `Opened`       | The ad became visible to the user.                     |
| `FailedToShow`            | A loaded ad failed to display.                         |
| `Dismissed` / `Closed`    | The user closed the full-screen ad or overlay.         |
| `Rewarded`                | The user earned the advertised reward.                 |
| `SizeChanged`             | Banner dimensions changed.                             |
| `AdImpression` / `AdPaid` | An impression was recorded. See revenue events below.  |

## Errors

`FailedToLoad` and `FailedToShow` listeners receive an [`AdMobError`](../README.md#admoberror).

## Impression-level revenue

Full-screen formats emit [`AdMobRevenueData`](../README.md#admobrevenuedata) on their `AdImpression` event. Banners emit the same payload on `AdPaid`. Banner `AdImpression` has no payload; it only signals that an impression was recorded.

## Per-format guides

- [App Open Ads](./app-open.md)
- [Banner Ads](./banner.md)
- [Interstitial Ads](./interstitial.md)
- [Rewarded Ads](./rewarded.md)

Method and enum signatures are in the [API reference](../README.md#api).
