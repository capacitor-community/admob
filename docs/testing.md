---
title: Testing and Debugging
---

# Testing and Debugging

## Test devices

Send an array of device Ids in `testingDevices` to use production like ads on your specified devices -> https://developers.google.com/admob/android/test-ads#enable_test_devices

Register test devices in `AdMobInitializationOptions`:

- `testingDevices` — an array of device IDs.
- `initializeForTesting` — set to `true` to register the listed devices.

Google provides [demo ad units](https://developers.google.com/admob/android/test-ads#demo_ad_units) that always return test ads.

## UMP testing

If you testing on real device, you have to set `debugGeography` and add your device ID to `testDeviceIdentifiers`. You can find your device ID with logcat (Android) or XCode (iOS).

```ts
import { AdMob, AdmobConsentDebugGeography } from '@capacitor-community/admob';

const consentInfo = await AdMob.requestConsentInfo({
  debugGeography: AdmobConsentDebugGeography.EEA,
  testDeviceIdentifiers: ['YOUR_DEVICE_ID'],
});
```

## Consent testing

**Note**: When testing, if you choose not consent (Manage -> Confirm Choices). The ads may not load/show. Even on testing enviroment. This is normal. It will work on Production so don't worry.

## Server-side verification

SSV callbacks are only fired on Production Adverts, therefore test Ads will not fire off your SSV callback.

For a mock SSV endpoint example, see [Rewarded Ads](./rewarded.md).
