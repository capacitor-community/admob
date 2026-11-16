# Banner Ads

Banner ads are rectangular ads that occupy a portion of an app's layout. They can stay on screen while the user interacts with the app, typically anchored at the top or bottom. Google's banner guides for [Android](https://developers.google.com/admob/android/banner) and [iOS](https://developers.google.com/admob/ios/banner) explain the format.

Call this after [initialize](https://docs.rdlabo.dev/projects/capacitor-admob/docs/configuration) and [consent](https://docs.rdlabo.dev/projects/capacitor-admob/docs/consent). This plugin draws the banner on the native screen (above the web view). Register listeners before calling `showBanner` so the first load and size events are not missed.

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
    // Inset your layout by size.height; see the next section.
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

<!-- !::showBanner:: -->

<!-- !::BannerAdOptions:: -->

<!-- !::BannerAdSize:: -->

<!-- !::BannerAdPosition:: -->

## Keep content out from under the banner

The banner is drawn on the native screen above the WebView. HTML layout does not move on its own. Inset your own root by `size.height` (logical pixels). Use padding or margin on the bottom for `BOTTOM_CENTER`, and on the top for `TOP_CENTER`.

```html
<main id="content">Your app</main>
```

```ts
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';

const content = document.getElementById('content');

await AdMob.addListener(BannerAdPluginEvents.SizeChanged, (size) => {
  if (!content) {
    return;
  }
  content.style.paddingBottom = size.height > 0 ? `${size.height}px` : '';
});
```

When the height is `0` (hidden, removed, or failed), clear the inset. Apply the same idea to whatever element fills the WebView in your framework.

See [Testing](https://docs.rdlabo.dev/projects/capacitor-admob/docs/testing) for `isTesting`.

## Lifecycle

- `hideBanner()` temporarily hides the current banner.
- `resumeBanner()` shows a hidden banner again.
- `removeBanner()` destroys it. Call `showBanner()` to create another one.

<!-- !::hideBanner:: -->

<!-- !::resumeBanner:: -->

<!-- !::removeBanner:: -->

Release listener handles when the owning screen is destroyed:

```ts
for (const handle of handles) {
  await handle.remove();
}
await AdMob.removeBanner();
```

Banner impression-level revenue is emitted on `BannerAdPluginEvents.AdPaid`. Full-screen formats emit the same `AdMobRevenueData` through their `AdImpression` event. See [Ad Events](https://docs.rdlabo.dev/projects/capacitor-admob/docs/events).

To move the existing Android banner, call `showBanner()` again with the same ad unit and request options and the new `position` or `margin`. The existing ad size is retained even if `adSize` is omitted. A layout-only change keeps the displayed ad and resolves without requesting another ad. Keep the same request options (such as `npa`) when moving it. Calling `showBanner()` with unchanged layout, or changing request options, retains the existing reload behavior. This does not create a second banner. Without a matching banner preloaded by `prepareBanner()`, use `removeBanner()` before switching to a different ad size or ad unit.

## Reserve space before loading an adaptive banner

Calculate the expected size without creating a banner or making an ad request:

```ts
const { height } = await AdMob.getAdaptiveBannerSize();
content.style.paddingBottom = `${height}px`;
await AdMob.showBanner(options);
```

The default width is the current available width with safe areas taken into account, using the same SDK calculation as `showBanner()` for adaptive banners. Dimensions are logical pixels (dp on Android, points on iOS). Recalculate after rotation or a change in available width, and continue using `SizeChanged` for the displayed size.

An optional `{ width: 300 }` calculates for a specific width in logical pixels; it does not change the width used by `showBanner()`. Invalid widths reject. This calculation is available on Android and iOS; the web implementation rejects as unavailable.

## Read the current banner size

`getBannerSize()` returns the displayed width and height in logical pixels, including when the initial size event was missed. It does not request or display an ad. Before loading, or while the banner is hidden, removed, or failed, both dimensions are zero.

```ts
const { width, height } = await AdMob.getBannerSize();
```

Keep listening to `BannerAdPluginEvents.SizeChanged` for subsequent changes.

## Preload without displaying

```ts
await AdMob.prepareBanner(options);
// Later, when the screen is ready:
await AdMob.showBanner(options);
```

`prepareBanner()` resolves when the next banner has loaded, leaving the current banner displayed. Use the same ad unit, size, and request options in `showBanner()` to display it without loading again; position and margin may differ. Without a reusable prepared banner, `showBanner()` works as usual.

Preparing again replaces the previous preparation. `removeBanner()` clears both the displayed and prepared banners. Pending preparation rejects when replaced or removed.

Preparation emits `Loaded` without `SizeChanged`. Showing a prepared banner emits `SizeChanged` only when the displayed dimensions change, without repeating `Loaded`.
