---
title: Testing
---

# Testing

Use test ads during development so you can click ads without charging advertisers or flagging the account for invalid traffic. Google's test ads guides for [Android](https://developers.google.com/admob/android/test-ads) and [iOS](https://developers.google.com/admob/ios/test-ads) explain demo ad units and test devices.

This plugin can request those demo units or register a device through `initialize`.

## Demo ad units

Google provides [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units) that always return test ads. Prefer these during development.

You can also set `isTesting: true` on banner, interstitial, rewarded, and rewarded interstitial requests. App Open ads have no `isTesting` option; pass a demo ad unit as `adId`.

## Test devices

To request production-like ads on a physical device without generating invalid traffic, register the device with `AdMob.initialize()`:

```ts
await AdMob.initialize({
  testingDevices: ['YOUR_TEST_DEVICE_ID'],
  initializeForTesting: true,
});
```

Find the device ID in the native logs after the first ad request:

- Android: Logcat, typically on the `Ads` tag (`Use RequestConfiguration.Builder.setTestDeviceIds(...)`).
- iOS: Xcode console (`To get test ads on this device, set:`).

See Google's [enable test devices](https://developers.google.com/admob/android/test-ads#enable_test_devices) guide.

## Consent debug geography

On a real device, set `debugGeography` and include the device ID in `testDeviceIdentifiers`. `EEA` makes the form behave as if the device were in the European Economic Area, so you can test GDPR messaging. Use this only on registered test devices.

```ts
import { AdMob, AdmobConsentDebugGeography } from '@capacitor-community/admob';

const consentInfo = await AdMob.requestConsentInfo({
  debugGeography: AdmobConsentDebugGeography.EEA,
  testDeviceIdentifiers: ['YOUR_TEST_DEVICE_ID'],
});
```

If you decline consent in the test form (Manage → Confirm Choices), ads may not load. That is expected in a test environment and does not predict production behavior after a user consents.

`resetConsentInfo()` is for tests only. See [Consent](./consent.md).

## Server-side verification

Server-side verification (SSV) callbacks fire only for production ads. Test ads will not hit your SSV endpoint. For a mock request example, see [Rewarded Ads](./rewarded.md).
