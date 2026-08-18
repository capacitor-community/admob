---
title: Installation
---

# Installation

If you use Capacitor 7:

```
% npm install --save @capacitor-community/admob@7
% npx cap update
```

## Google Mobile Ads SDK compatibility

To preserve behavior for users of the current major version, this plugin continues to use Google Mobile Ads SDK APIs that are deprecated but still supported. Replacing those APIs can change banner sizing and age-restricted treatment behavior, so that migration is deferred until the next major release.

Migration to the [GMA Next-Gen SDK for Android](https://developers.google.com/admob/android/next-gen) is also deferred until the next major release because it requires breaking changes to SDK initialization, ad requests, and mediation integration.

Android continues to use GMA SDK (Legacy) 25.4.x. On iOS, both Swift Package Manager and CocoaPods are fixed to GMA SDK 13.6.0 until CocoaPods support is removed in the next major release.

## Android configuration

In file `android/app/src/main/AndroidManifest.xml`, add the following XML elements under `<manifest><application>` :

```xml
<meta-data
 android:name="com.google.android.gms.ads.APPLICATION_ID"
 android:value="@string/admob_app_id"/>
```

In file `android/app/src/main/res/values/strings.xml` add the following lines :

```xml
<string name="admob_app_id">[APP_ID]</string>
```

Don't forget to replace `[APP_ID]` by your AdMob application Id.

### Variables

This plugin will use the following project variables (defined in your app's `variables.gradle` file):

- `playServicesAdsVersion` version of `com.google.android.gms:play-services-ads` (default: `25.4.+`)
- `androidxCoreKTXVersion`: version of `androidx.core:core-ktx` (default: `1.15.0`)

## iOS configuration

Add the following in the `ios/App/App/info.plist` file inside of the outermost `<dict>`:

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
<string>[Why you use NSUserTracking. ex: This identifier will be used to deliver personalized ads to you.]</string>
```

Don't forget to replace `[APP_ID]` by your AdMob application Id.

## Troubleshooting

### If you have error:

> [error] Error running update: Analyzing dependencies
> [!] CocoaPods could not find compatible versions for pod "Google-Mobile-Ads-SDK":

You should run `pod repo update` ;
