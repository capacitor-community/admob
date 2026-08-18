---
title: Configuration
---

# Configuration

Call `initialize` once before requesting consent information or loading ads. Native application IDs belong in AndroidManifest / Info.plist; see [Installation](./installation.md).

```ts
import { AdMob } from '@capacitor-community/admob';

await AdMob.initialize({
  testingDevices: ['YOUR_TEST_DEVICE_ID'],
  initializeForTesting: true,
});
```

Use Google [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units) while developing. If you must test production-like ads, register test device IDs as shown above. Details are in [Testing](./testing.md).

## Initialization options

| Option                         | Type                | Description                                                                                          |
| ------------------------------ | ------------------- | ---------------------------------------------------------------------------------------------------- |
| `testingDevices`               | `string[]`          | Device IDs to treat as test devices when `initializeForTesting` is `true`.                           |
| `initializeForTesting`         | `boolean`           | Registers `testingDevices`. Defaults to `false`.                                                     |
| `tagForChildDirectedTreatment` | `boolean`           | COPPA child-directed treatment tag.                                                                  |
| `tagForUnderAgeOfConsent`      | `boolean`           | TFUA tag for users under the age of consent in Europe.                                               |
| `maxAdContentRating`           | `MaxAdContentRating` | Maximum content rating applied to all ad requests. Ads above this rating are excluded.              |

Request-level options such as `isTesting`, `npa`, and `immersiveMode` are set on each ad request, not on `initialize`. See the per-format guides.

After initialization, request privacy consent before loading ads. See [Consent](./consent.md).
