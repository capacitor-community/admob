---
title: Initialization
---

# Initialization

## Common setup

### Initialize AdMob

```ts
import { AdMob, AdmobConsentStatus } from '@capacitor-community/admob';

export async function initialize(): Promise<void> {
  await AdMob.initialize({
  });

  const [trackingInfo, consentInfo] = await Promise.all([
    AdMob.trackingAuthorizationStatus(),
    AdMob.requestConsentInfo(),
  ]);

  if (trackingInfo.status === 'notDetermined') {
    /**
     * If you want to explain TrackingAuthorization before showing the iOS dialog,
     * you can show the modal here.
     * ex)
     * const modal = await this.modalCtrl.create({
     *   component: RequestTrackingPage,
     * });
     * await modal.present();
     * await modal.onDidDismiss();  // Wait for close modal
     **/

    await AdMob.requestTrackingAuthorization();
  }

  const authorizationStatus = await AdMob.trackingAuthorizationStatus();
  if (
    authorizationStatus.status === 'authorized' &&
    consentInfo.isConsentFormAvailable &&
    consentInfo.status === AdmobConsentStatus.REQUIRED
  ) {
    await AdMob.showConsentForm();
  }
}
```

Send an array of device Ids in `testingDevices` to use production like ads on your specified devices -> https://developers.google.com/admob/android/test-ads#enable_test_devices

### User Message Platform (UMP)

To use UMP, you must [create your GDPR messages](https://support.google.com/admob/answer/10113207?hl=en&ref_topic=10105230&sjid=6731900490614517032-AP).

You may need to [setup IDFA messages](https://support.google.com/admob/answer/10115027?hl=en), it will work along with GDPR messages and will show when users are not in EEA and UK.

Example of how to use UMP.

```ts
import { AdMob } from '@capacitor-community/admob';

private canShowAds: boolean | null = null;

async showConsent() {
  let consentInfo = await AdMob.requestConsentInfo();
  if (!consentInfo.canRequestAds) {
    consentInfo = await AdMob.showConsentForm();
    this.canShowAds = consentInfo.canRequestAds;
  }
}
```

To let users manage their privacy options at any time, show the privacy options form.

```ts
import { AdMob } from '@capacitor-community/admob';

showPrivacyOptionsForm() {
    AdMob.showPrivacyOptionsForm();
}
```

If you testing on real device, you have to set `debugGeography` and add your device ID to `testDeviceIdentifiers`. You can find your device ID with logcat (Android) or XCode (iOS).

```ts
import { AdMob, AdmobConsentDebugGeography } from '@capacitor-community/admob';

const consentInfo = await AdMob.requestConsentInfo({
  debugGeography: AdmobConsentDebugGeography.EEA,
  testDeviceIdentifiers: ['YOUR_DEVICE_ID'],
});
```

**Note**: When testing, if you choose not consent (Manage -> Confirm Choices). The ads may not load/show. Even on testing enviroment. This is normal. It will work on Production so don't worry.

Before requesting an ad, complete these steps in order:

1. Call `AdMob.initialize()`.
2. Call `AdMob.requestConsentInfo()`.
3. If required, call `AdMob.showConsentForm()`.
4. Load or show the ad format you need.
