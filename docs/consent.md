---
title: Consent
---

# Consent

Google's User Messaging Platform (UMP) SDK is the privacy and messaging tool for gathering consent before you request ads. Google's UMP guides for [Android](https://developers.google.com/admob/android/privacy) and [iOS](https://developers.google.com/admob/ios/privacy) explain the flow.

This plugin exposes UMP and iOS App Tracking Transparency through one API. Before using UMP, [create your GDPR messages](https://support.google.com/admob/answer/10113207) in AdMob. You may also [set up Identifier for Advertisers (IDFA) messages](https://support.google.com/admob/answer/10115027). When IDFA messages are published, UMP presents them and the App Tracking Transparency prompt; do not also call `requestTrackingAuthorization()`.

## Recommended order

1. Call `AdMob.initialize()`. See [Configuration](./configuration.md).
2. Call `AdMob.requestConsentInfo()`.
3. If required, call `AdMob.showConsentForm()`.
4. Load ads only when `consentInfo.canRequestAds` is `true`.

```ts
import { AdMob, AdmobConsentStatus } from '@capacitor-community/admob';

await AdMob.initialize();

let consentInfo = await AdMob.requestConsentInfo();
if (consentInfo.isConsentFormAvailable && consentInfo.status === AdmobConsentStatus.REQUIRED) {
  consentInfo = await AdMob.showConsentForm();
}

if (consentInfo.canRequestAds) {
  // Ads may now be requested.
}
```

Use `canRequestAds` as the decision point. A consent form may be unavailable or unnecessary depending on the user and the messages you configured.

## iOS tracking authorization

If you are **not** using UMP IDFA messages, request App Tracking Transparency yourself when the status is `notDetermined`. Skip this section when Identifier for Advertisers messages are configured in AdMob; UMP shows that prompt.

```ts
const tracking = await AdMob.trackingAuthorizationStatus();
if (tracking.status === 'notDetermined') {
  /**
   * If you want to explain tracking before the iOS dialog,
   * present your own UI here, then continue.
   */
  await AdMob.requestTrackingAuthorization();
}
```

`requestTrackingAuthorization()` does nothing on Android, web, and iOS versions before 14.

## Privacy options

If your privacy message requires an in-app entry point, expose a settings action that calls:

```ts
await AdMob.showPrivacyOptionsForm();
```

## Reset consent

`resetConsentInfo()` is intended for testing. Do not use it to clear a production user's consent choice.

For debug geography and test device IDs, see [Testing](./testing.md).
