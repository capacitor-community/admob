# Configuration

Call the plugin's `initialize` once before requesting ads. You do not start the native SDK yourself.

Native application IDs belong in AndroidManifest / Info.plist; see [Installation](../README.md#installation).

```ts
import { AdMob } from '@capacitor-community/admob';

await AdMob.initialize();
```

During development, prefer Google [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units). To test production-like ads on a physical device, register that device as described in [Testing](./testing.md). Do not ship `initializeForTesting: true` in production.

Initialization fields are defined on [`AdMobInitializationOptions`](../README.md#admobinitializationoptions). Per-ad options such as `isTesting`, `npa` (non-personalized ads), and `immersiveMode` (hide Android system bars on a full-screen ad) are set on each ad request, not on `initialize`. See the per-format guides.

After initialization, request privacy consent before loading ads. See [Consent](./consent.md).
