---
title: Ad Events
---

# Ad Events

Register event listeners before loading or showing an ad so that the first lifecycle and impression events are not missed.

## Adding and removing listeners

Use `AdMob.addListener` and call `remove()` on the returned handle:

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
| `Dismissed` / `Closed`    | The user closed the full-screen or overlay.            |
| `AdImpression` / `AdPaid` | An impression was recorded with revenue data.          |

## Error handling

`FailedToLoad` and `FailedToShow` listeners receive an `AdMobError`:

| Prop      | Type     | Description                     |
| --------- | -------- | ------------------------------- |
| `code`    | `number` | The error code from the SDK.    |
| `message` | `string` | A message describing the error. |

## Per-format event references

- [App Open Ads](./app-open.md)
- [Banner Ads](./banner.md)
- [Interstitial Ads](./interstitial.md)
- [Rewarded Ads](./rewarded.md)

For full event details, see the [API reference](../README.md#api).
