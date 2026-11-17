# Migration Guide

## Changes in 8.2.0

### Capacitor 8.5 or later is required

Update `@capacitor/core`, `@capacitor/cli`, and the native platform packages you use (`@capacitor/android` / `@capacitor/ios`) to 8.5 or later within v8, then run `npx cap sync`.

Older Capacitor versions can consume Android window insets before they reach a banner. Update Capacitor to use its current safe-area handling. Follow the [Capacitor 8.5 update guide](https://capacitorjs.com/docs/updating/8-5) for native project migration steps.

### Android initialization can reject

`AdMob.initialize()` now waits for the native banner parent view. If it does not appear within 5 seconds, initialization rejects; an unavailable activity/content view can fail immediately. This also applies to apps that only use full-screen ads. Previously, a missing child view could let initialization resolve and cause later banner requests to crash.

Handle initialization errors without blocking app startup. Retry when the native view is available. See [Configuration](https://docs.rdlabo.dev/projects/capacitor-admob/docs/configuration) for an example.

### iOS revenue values now use micros

The `valueMicros` field now correctly reports millionths of a currency unit for banners, interstitials, rewarded ads, rewarded interstitials, and app-open ads. For example, a value of `0.0012` currency units previously produced `0`; it now produces `1200`. Android values and event names are unchanged.

Divide `valueMicros` by `1_000_000` to obtain currency units on either platform. Review any iOS-specific workarounds in your analytics pipeline. Historical iOS values were truncated before conversion, so multiplying those stored values cannot recover the lost fractional revenue.

### Consent before SDK initialization

On iOS, `showConsentForm()` and `showPrivacyOptionsForm()` can now be called before `AdMob.initialize()`, matching Android. Request consent information, present a form if required, then initialize the Mobile Ads SDK and load ads only when `canRequestAds` is true. See [Consent](https://docs.rdlabo.dev/projects/capacitor-admob/docs/consent) for the complete sequence. Existing initialize-first integrations remain callable, but should adopt this order.

### Ad load errors preserve native codes

`prepareInterstitial()`, `prepareRewardVideoAd()`, and `prepareRewardInterstitialAd()` now reject SDK load failures with a string `code` and the native error message. Codes are platform-specific, not normalized across Android and iOS. `FailedToLoad` event codes remain numbers; on iOS they now contain the actual SDK code instead of a fixed `0`.

Update error handlers that match the iOS message `Loading failed` or assume an event code of `0`. Use the platform's SDK error definitions when handling no-fill and other failures.

### Rewarded ad click event

`RewardAdPluginEvents.adClicked` is a new optional listener for rewarded ads on Android and iOS. Its event string is `onRewardedVideoAdClicked`. Clicks are separate from earned rewards; continue granting rewards only from the `Rewarded` event or the show result, once. See [Rewarded Ads](https://docs.rdlabo.dev/projects/capacitor-admob/docs/rewarded).

### AGP 9 build compatibility

The Android library now references `proguard-android-optimize.txt`, avoiding AGP 9's rejection of the older default file. This does not enable library minification or upgrade your project's AGP. Other AGP 9 migration steps still apply to the host app.

The version-by-version steps under “Breaking changes from earlier versions” apply to releases before v8.

## Google Mobile Ads SDK versions

This major version keeps Google Mobile Ads SDK APIs that are deprecated but still supported. Replacing them can change banner sizing and age-restricted treatment, so that work waits for the next major.

Google's [Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen) also waits for the next major: it changes SDK initialization, ad requests, and mediation.

Pinned versions: Android 25.4.x, iOS 13.11.0 (Swift Package Manager and CocoaPods). CocoaPods support is planned to be removed in the next major.

Android remains on 25.4.x because [SDK 25.5.0 raises the minimum Android API level](https://developers.google.com/admob/android/rel-notes). The iOS update from 13.6.0 to 13.11.0 keeps the plugin's existing platform and toolchain requirements; no public plugin API changes are required.

## Breaking changes from earlier versions

### 1.1.0

- Prepare for iOS 14+
- In file `ios/App/App/AppDelegate.swift` remove the following:

```diff
- import GoogleMobileAds

  @UIApplicationMain
  class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
-     // Override point for customization after application launch.
-     GADMobileAds.sharedInstance().start(completionHandler: nil)
```

### 0.2.13

- isTest: 'LIVE' | 'TESTING' => boolean

### 0.2.12

**app.component.ts**

```ts
import { Plugins } from '@capacitor/core';

const { AdMob } = Plugins;

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  constructor() {
    // Initialize AdMob for your Application
    +AdMob.initialize('[APP_ID]');
    -AdMob.initialize();
  }
}
```

**admob.component.ts**

```ts
    import { Plugins } from '@capacitor/core';
    import { AdOptions, AdSize, AdPosition } from '@rdlabo/capacitor-admob';

    const { AdMob } = Plugins;

    @Component({
      selector: 'admob',
      templateUrl: 'admob.component.html',
      styleUrls: ['admob.component.scss']
    })
    export class AdMobComponent {

        const options: AdOptions = {
            adId: 'YOUR ADID',
            adSize: AdSize.BANNER,
            position: AdPosition.BOTTOM_CENTER,
-           margin: '0',
+           margin: 0,
        }

        constructor(){
            // Show Banner Ad
            AdMob.showBanner(this.options)
            .then(
                (value) => {
                    console.log(value);  // true
                },
                (error) => {
                    console.error(error); // show error
                }
            );

            // Subscibe Banner Event Listener
            AdMob.addListener('onAdLoaded', (info: boolean) => {
                 console.log("Banner Ad Loaded");
            });

+           // Get Banner Size
+           AdMob.addListener('onAdSize', (info: boolean) => {
+                console.log(info);
+           });
        }
    }
```
