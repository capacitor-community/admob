# Native ads research preview

Native ads are rendered with Google Mobile Ads SDK views owned by this plugin. Your app reserves an HTML slot and supplies a stable key; it does not implement a `NativeAdView` in Kotlin or Swift and does not render ad assets in JavaScript.

This API is an unsupported research preview for iOS and Android device validation. Do not ship it in production and do not describe virtual scrolling as supported yet. A native ad is placed above the WebView, so a pan beginning on the ad may not reach the WebView's scroll container. The implementation hides overlays after the WebView reports motion, but that cannot solve the initial gesture-routing problem. Production support is gated on real-device acceptance tests for this behavior.

Promotion from research preview also requires VoiceOver and TalkBack ordering tests, fast-fling/recycling tests, modal/tab/background lifecycle tests, rotation and keyboard tests, and Google Native Ad Validator checks on both platforms.

Browsers and PWAs are unsupported. `NativeAdFeed.create()` rejects instead of leaving an empty ad slot.

## Create a feed

Create one manager for each visible feed screen after initializing AdMob and obtaining consent.

```ts
import { NativeAdFeed, NativeAdPluginEvents, NativeAdTemplate } from '@capacitor-community/admob';

const nativeAds = await NativeAdFeed.create({
  feedId: 'home-feed',
  template: NativeAdTemplate.Medium,
  isTesting: true,
  style: {
    backgroundColor: '#ffffff',
    cornerRadius: 12,
    headlineColor: '#111827',
    callToActionBackgroundColor: '#2563eb',
  },
});

const paidHandle = await nativeAds.addListener(NativeAdPluginEvents.AdPaid, (event) => {
  console.log(event.slotKey, event.valueMicros, event.currencyCode);
});
```

Use the framework-independent element in ordinary or virtualized markup. `slot-key` must identify the logical ad item, not its array index and not the recycled DOM node.

```html
<capacitor-admob-native feed-id="home-feed" slot-key="sponsored-after-article-42"></capacitor-admob-native>
```

The element reserves `320px` for `Medium` and `120px` for `Small` only when author CSS does not provide a height. Author `display: none` and explicit height rules are respected. `Small` slots must be at least `120×120px`; `Medium` slots must be at least `144×300px`, leaving a policy-compliant `120×120` media area after template padding. Smaller slots are not loaded. Do not animate or dynamically measure the slot height.

If a framework does not accept custom elements, attach an ordinary element instead:

```ts
nativeAds.attach('sponsored-after-article-42', element);

// Before the element is destroyed or reused for another logical item:
nativeAds.detach(element);
```

Destroy the manager with its screen:

```ts
await paidHandle.remove();
await nativeAds.destroy();
```

## Virtual-scroll integration experiments

The stable-key lifecycle is designed for later validation with Angular CDK virtual scroll, React Virtuoso, and Vue Virtual Scroller. These integrations are examples for experiments, not a support claim:

- Put the logical ad ID in `slot-key` (Angular binding, React prop/ref, or Vue `:slot-key`). Never use the rendered index.
- Keep the ad row at a fixed height and include that height in the virtual scroller's item-size calculation.
- When a library recycles a row, update the key or call `detach` before calling `attach` with the new key. The plugin hides the old generation before loading or showing the new one.
- Use a single vertical scroll root. Nested scroll containers, horizontal virtual lists, sticky/transformed ancestors, and animated row heights are not in the initial support profile.

For Angular, add `CUSTOM_ELEMENTS_SCHEMA` to the standalone component or NgModule that owns the feed, then bind attributes (not properties) inside the virtual row:

```html
<capacitor-admob-native feed-id="home-feed" [attr.slot-key]="item.stableAdKey"></capacitor-admob-native>
```

With React, `createElement` keeps custom dash-cased attributes type-safe without adding a framework dependency to the plugin:

```tsx
import { createElement } from 'react';

const NativeAdRow = ({ slotKey }: { slotKey: string }) =>
  createElement('capacitor-admob-native', {
    'feed-id': 'home-feed',
    'slot-key': slotKey,
  });
```

For Vue, mark the tag as a custom element in the Vue compiler and bind the stable key:

```ts
// vite.config.ts
vue({
  template: {
    compilerOptions: {
      isCustomElement: (tag) => tag === 'capacitor-admob-native',
    },
  },
});
```

```html
<capacitor-admob-native feed-id="home-feed" :slot-key="item.stableAdKey" />
```

Placement updates are batched and scoped to a feed session. Native overlays are hidden after WebView scrolling begins and restored after the viewport settles. A feed keeps at most three native ads, including attached offscreen slots; additional visible slots wait for capacity. At most two feed managers may be active, which bounds the plugin-wide total at six native ads. Ads are not automatically refreshed. Failed loads are not silently retried. Call `reload(slotKey)` only at an explicit product-defined retry or refresh point.

## Layout and overlay lifecycle

Call `invalidateLayout()` after application-driven layout changes that can move a slot without scrolling, such as expanding an accordion. Slot size changes and captured image-load events are detected automatically.

Native ads sit above WebView content and cannot infer that an Ionic modal, popover, menu, loading indicator, or route transition covers the slot. Hide the feed before presenting an overlay and resume it after dismissal:

```ts
await nativeAds.pause();
await modal.present();
await modal.onDidDismiss();
nativeAds.resume();
```

Call `destroy()` when leaving a screen. Await `pause()` before presenting an overlay; it rejects if the native hide update fails. `resume()` waits for the viewport to settle before showing eligible placements again. `invalidateLayout()` likewise resolves only after the stale placement has been hidden.

## API

| Member                                         | Purpose                                                        |
| ---------------------------------------------- | -------------------------------------------------------------- |
| `NativeAdFeed.create(options)`                 | Starts a native feed session. Rejects outside iOS and Android. |
| `feedId`                                       | Normalized feed identifier used by the custom element.         |
| `addListener(event, listener)`                 | Adds a listener filtered to this feed session.                 |
| `attach(slotKey, element)` / `detach(element)` | Advanced lifecycle for an ordinary element.                    |
| `reload(slotKey)`                              | Explicitly removes and reloads one registered slot.            |
| `pause(): Promise<void>` / `resume(): void`    | Hides placements during Web overlays or inactive page states.  |
| `invalidateLayout(): Promise<void>`            | Hides and remeasures after an application-driven reflow.       |
| `destroy()`                                    | Removes listeners and all native resources for the session.    |

`NativeAdFeedOptions` contains only `feedId`, `template`, `style`, `isTesting`, and `npa`. `isTesting: true` is required, and both native implementations always use Google's platform test ad unit during this preview. `feedId` and every `slotKey` must be non-empty and stable; reuse the same feed IDs across WebView reloads so stale native sessions can be replaced safely.

## Rendering and policy boundary

The `Small` and `Medium` templates, attribution, AdChoices, media, and clickable asset registration are owned by the plugin. The public style API intentionally exposes a limited set of cross-platform tokens instead of arbitrary native layouts or HTML asset rendering. This keeps Google SDK impression and click handling inside the native SDK.

Style colors use CSS-style `#RRGGBB` or `#RRGGBBAA` values on both platforms. Dimensions use logical pixels; font sizes use points on iOS and `sp` on Android. Invalid colors fall back to the template defaults. Negative dimensions are clamped to zero; headline, body, and call-to-action fonts are clamped to `12–24`, `10–18`, and `12–18` respectively.

Follow Google's native ad policies and implementation guidance for [Android](https://developers.google.com/admob/android/native/advanced) and [iOS](https://developers.google.com/admob/ios/native/advanced).
