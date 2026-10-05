# Configuration

After collecting consent and checking `canRequestAds`, call the plugin's `initialize` once before requesting ads. You do not start the native SDK yourself.

Native application IDs belong in AndroidManifest / Info.plist; see [Installation](https://docs.rdlabo.dev/projects/capacitor-admob/docs/readme#installation).

```ts
import { AdMob } from '@capacitor-community/admob';

try {
  await AdMob.initialize();
} catch (error) {
  console.error('AdMob initialization failed', error);
  // Skip ad requests for now. Retry after the app's native view is available.
}
```

On Android, initialization also waits for the native banner parent view, even if your app only uses full-screen ads. It rejects if the activity/content view is unavailable, or if the parent does not appear within 5 seconds. Handle this rejection so an ad initialization failure does not prevent the rest of your app from starting. You can retry once the native view is available.

A resolved `initialize()` does not mean an ad has loaded. Use each format's load method and events to check ad readiness.

<!-- !::initialize:: -->

<!-- !::AdMobInitializationOptions:: -->

During development, prefer Google [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units). To test production-like ads on a physical device, register that device as described in [Testing](https://docs.rdlabo.dev/projects/capacitor-admob/docs/testing). Do not ship `initializeForTesting: true` in production.

Per-ad options such as `isTesting`, `npa` (non-personalized ads), and `immersiveMode` (hide Android system bars on a full-screen ad) are set on each ad request, not on `initialize`. See the per-format guides.

Collect privacy consent before initialization and loading ads. See [Consent](https://docs.rdlabo.dev/projects/capacitor-admob/docs/consent).
