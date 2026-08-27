# Banner Ads

Banner ads are rectangular ads that occupy a portion of an app's layout. They can stay on screen while the user interacts with the app, typically anchored at the top or bottom. Google's banner guides for [Android](https://developers.google.com/admob/android/banner) and [iOS](https://developers.google.com/admob/ios/banner) explain the format.

Call this after [initialize](./configuration.md) and [consent](./consent.md). This plugin draws the banner on the native screen (above the web view). Register listeners before calling `showBanner` so the first load and size events are not missed.

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

See [Testing](./testing.md) for `isTesting`.

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

Banner impression-level revenue is emitted on `BannerAdPluginEvents.AdPaid`. Full-screen formats emit the same `AdMobRevenueData` through their `AdImpression` event. See [Ad Events](./events.md).
