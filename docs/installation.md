---
title: Installation
---

# Installation

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

## Google Mobile Ads SDK compatibility

To preserve behavior for users of the current major version, this plugin continues to use Google Mobile Ads SDK APIs that are deprecated but still supported. Replacing those APIs can change banner sizing and age-restricted treatment behavior, so that migration is deferred until the next major release.

Migration to the [GMA Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen) is also deferred until the next major release because it requires breaking changes to SDK initialization, ad requests, and mediation integration.

Android continues to use GMA SDK (Legacy) 25.4.x. On iOS, both Swift Package Manager and CocoaPods are fixed to GMA SDK 13.6.0 until CocoaPods support is removed in the next major release.

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

This plugin uses the following project variables (defined in your app's `variables.gradle` file):

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

## Troubleshooting

If CocoaPods cannot resolve `Google-Mobile-Ads-SDK`:

```text
[error] Error running update: Analyzing dependencies
[!] CocoaPods could not find compatible versions for pod "Google-Mobile-Ads-SDK":
```

Run `pod repo update` in `ios/`, then `npx cap sync ios` again.
