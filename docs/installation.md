# Installation

This plugin already ships Google Mobile Ads SDK. Install the package, then add your AdMob **application** ID in AndroidManifest / Info.plist. Google's Get started guides for [Android](https://developers.google.com/admob/android/quick-start) and [iOS](https://developers.google.com/admob/ios/quick-start) explain app IDs and SKAdNetwork identifiers; do not add a second Mobile Ads dependency.

This plugin targets `@capacitor-community/admob` **v8** and Capacitor 8. It supports iOS 15 or later and Android API 24 or later.

```bash
npm install @capacitor-community/admob
npx cap sync
```

If you still use Capacitor 7, install the previous major:

```bash
npm install @capacitor-community/admob@7
npx cap sync
```

Then add the Android and iOS application ID entries below. For `AdMob.initialize()` options, see [Configuration](./configuration.md).

## Google Mobile Ads SDK versions

This major version pins Google Mobile Ads SDK **25.4.x** on Android and **13.6.0** on iOS (Swift Package Manager and CocoaPods). Leave those versions unless you have a specific need. Google's [Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen) waits until the next plugin major. See [Migration](./migration.md) for the policy behind the pins.

## Android configuration

In `android/app/src/main/AndroidManifest.xml`, add the following under `<application>`:

```xml
<meta-data
  android:name="com.google.android.gms.ads.APPLICATION_ID"
  android:value="@string/admob_app_id" />
```

In `android/app/src/main/res/values/strings.xml`:

```xml
<string name="admob_app_id">[APP_ID]</string>
```

Replace `[APP_ID]` with your AdMob **application** ID, not an ad unit ID.

### Variables

You can leave these unset. Override them in your app's `variables.gradle` only when you need a specific artifact version:

| Variable                       | Artifact                                         | Default  |
| ------------------------------ | ------------------------------------------------ | -------- |
| `playServicesAdsVersion`       | `com.google.android.gms:play-services-ads`       | `25.4.+` |
| `userMessagingPlatformVersion` | `com.google.android.ump:user-messaging-platform` | `4.0.0`  |
| `androidxCoreKTXVersion`       | `androidx.core:core-ktx`                         | `1.15.0` |

## iOS configuration

Add the following inside the outermost `<dict>` in `ios/App/App/Info.plist`:

```xml
<key>GADIsAdManagerApp</key>
<true/>
<key>GADApplicationIdentifier</key>
<string>[APP_ID]</string>
<key>SKAdNetworkItems</key>
<array>
  <dict>
    <key>SKAdNetworkIdentifier</key>
    <string>cstr6suwn9.skadnetwork</string>
  </dict>
</array>
<key>NSUserTrackingUsageDescription</key>
<string>This identifier will be used to deliver personalized ads to you.</string>
```

Replace `[APP_ID]` with your AdMob application ID, and describe your actual tracking use in `NSUserTrackingUsageDescription`.

The `SKAdNetworkItems` snippet includes Google's own identifier. Add the other IDs from Google's [iOS setup guide](https://developers.google.com/admob/ios/quick-start#update_your_infoplist).

## Troubleshooting

If CocoaPods cannot resolve `Google-Mobile-Ads-SDK`:

```text
[error] Error running update: Analyzing dependencies
[!] CocoaPods could not find compatible versions for pod "Google-Mobile-Ads-SDK":
```

Run `pod repo update` in `ios/`, then `npx cap sync ios` again.
