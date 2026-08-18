---
title: Migration Guide
---

# Migration Guide

## SDK compatibility

### Google Mobile Ads SDK compatibility

To preserve behavior for users of the current major version, this plugin continues to use Google Mobile Ads SDK APIs that are deprecated but still supported. Replacing those APIs can change banner sizing and age-restricted treatment behavior, so that migration is deferred until the next major release.

Migration to the [GMA Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen) is also deferred until the next major release because it requires breaking changes to SDK initialization, ad requests, and mediation integration.

Android continues to use GMA SDK (Legacy) 25.4.x. On iOS, both Swift Package Manager and CocoaPods are fixed to GMA SDK 13.6.0 until CocoaPods support is removed in the next major release.

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
