---
title: Consent
---

# Consent

Request privacy information before loading ads. The plugin exposes Google's User Messaging Platform (UMP) and iOS App Tracking Transparency through one API.

Before using UMP, [create your GDPR messages](https://support.google.com/admob/answer/10113207) in AdMob. You may also need to [set up IDFA messages](https://support.google.com/admob/answer/10115027); they work alongside GDPR messages and can appear for users outside the EEA and UK.

## Recommended order

1. Call `AdMob.initialize()`. See [Configuration](./configuration.md).
2. On iOS, resolve App Tracking Transparency if the status is `notDetermined`.
3. Call `AdMob.requestConsentInfo()`.
4. If required, call `AdMob.showConsentForm()`.
5. Load ads only when `consentInfo.canRequestAds` is `true`.

```ts
import { AdMob, AdmobConsentStatus } from '@capacitor-community/admob';

await AdMob.initialize();

const tracking = await AdMob.trackingAuthorizationStatus();
if (tracking.status === 'notDetermined') {
  /**
   * If you want to explain tracking before the iOS dialog,
   * present your own UI here, then continue.
   */
  await AdMob.requestTrackingAuthorization();
}

let consentInfo = await AdMob.requestConsentInfo();
if (consentInfo.isConsentFormAvailable && consentInfo.status === AdmobConsentStatus.REQUIRED) {
  consentInfo = await AdMob.showConsentForm();
}

if (consentInfo.canRequestAds) {
  // Ads may now be requested.
}
```

Use `canRequestAds` as the decision point. A consent form may be unavailable or unnecessary depending on the user and the messages you configured.

`requestTrackingAuthorization()` is a no-op on Android, web, and iOS versions before 14.

## Privacy options

If your privacy message requires an in-app entry point, expose a settings action that calls:

```ts
await AdMob.showPrivacyOptionsForm();
```

## Reset consent

`resetConsentInfo()` is intended for testing. Do not use it to clear a production user's consent choice.

For debug geography and test device IDs, see [Testing](./testing.md).
