---
title: Configuration
---

# Configuration

Call `initialize` once before requesting consent information or loading ads. Native application IDs belong in AndroidManifest / Info.plist; see [Installation](./installation.md).

```ts
import { AdMob } from '@capacitor-community/admob';

await AdMob.initialize();
```

During development, prefer Google [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units). To test production-like ads on a physical device, register that device as described in [Testing](./testing.md). Do not ship `initializeForTesting: true` in production.

## Initialization options

| Option                         | Type                 | Description                                                                                          |
| ------------------------------ | -------------------- | ---------------------------------------------------------------------------------------------------- |
| `testingDevices`               | `string[]`           | Device IDs to treat as test devices when `initializeForTesting` is `true`.                           |
| `initializeForTesting`         | `boolean`            | Registers `testingDevices`. Defaults to `false`. Use only while developing.                          |
| `tagForChildDirectedTreatment` | `boolean`            | Children's Online Privacy Protection Act (COPPA) child-directed treatment tag.                       |
| `tagForUnderAgeOfConsent`      | `boolean`            | Tag For Users under the Age of Consent in Europe (TFUA).                                             |
| `maxAdContentRating`           | `MaxAdContentRating` | Maximum content rating applied to all ad requests. Ads above this rating are excluded.               |

Per-ad options such as `isTesting`, `npa` (non-personalized ads), and `immersiveMode` are set on each ad request, not on `initialize`. See the per-format guides.

After initialization, request privacy consent before loading ads. See [Consent](./consent.md).
