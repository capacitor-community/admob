<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">AdMob</h3>
<p align="center"><strong><code>@capacitor-community/admob</code></strong></p>
<p align="center">
  Capacitor community plugin for native AdMob.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2021?style=flat-square" />
  <!-- <a href="https://github.com/capacitor-community/example/actions?query=workflow%3A%22CI%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/example/CI?style=flat-square" /></a> -->
  <a href="https://www.npmjs.com/package/@capacitor-community/admob"><img src="https://img.shields.io/npm/l/@capacitor-community/admob?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/admob"><img src="https://img.shields.io/npm/dw/@capacitor-community/admob?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/admob"><img src="https://img.shields.io/npm/v/@capacitor-community/admob?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-4-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

## Maintainers

| Maintainer          | GitHub                              | Social                                | Sponsoring Company                             |
| ------------------- | ----------------------------------- | ------------------------------------- | ---------------------------------------------- |
| Masahiko Sakakibara | [rdlabo](https://github.com/rdlabo) | [@rdlabo](https://twitter.com/rdlabo) | RELATION DESIGN LABO, GENERAL INC. ASSOCIATION |

Maintenance Status: Actively Maintained

## Demo

[Demo code is here.](https://github.com/capacitor-community/admob/tree/feat/v3/demo)

### Screenshots

|             |                Banner                |                Interstitial                |                Reward                |
| :---------- | :----------------------------------: | :----------------------------------------: | :----------------------------------: |
| **iOS**     | ![](demo/screenshots/ios_banner.png) | ![](demo/screenshots/ios_interstitial.png) | ![](demo/screenshots/ios_reward.png) |
| **Android** | ![](demo/screenshots/md_banner.png)  | ![](demo/screenshots/md_interstitial.png)  | ![](demo/screenshots/md_reward.png)  |

## Installation

```
% npm install --save @capacitor-community/admob@next
% npx cap update
```

## Android configuration

In file `android/app/src/main/java/**/**/MainActivity.java`, add the plugin to the initialization list:

```java
public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlugin(com.getcapacitor.community.admob.AdMob.class);
    }
}
```

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

## iOS configuration

Add the following in the `ios/App/App/info.plist` file inside of the outermost `<dict>`:

```xml
<key>GADIsAdManagerApp</key>
<true/>

<key>GADApplicationIdentifier</key>
<string>[APP_ID]</string>

<key>NSUserTrackingUsageDescription</key>
<string>[Why you use NSUserTracking. ex: This identifier will be used to deliver personalized ads to you.]</string>
```

Don't forget to replace `[APP_ID]` by your AdMob application Id.

## Initialize

```ts
initialize(options: { requestTrackingAuthorization?: boolean , testingDevices?: string[]}): Promise<{ value: boolean }>
```

You can use option `requestTrackingAuthorization`. This change permission to require `AppTrackingTransparency` in iOS >= 14:
https://developers.google.com/admob/ios/ios14

Default value is `true`. If you don't want to track, set requestTrackingAuthorization `false`.

Send and array of device Ids in `testingDevices? to use production like ads on your specified devices -> https://developers.google.com/admob/android/test-ads#enable_test_devices

## APIs

### BANNER

#### Angular

```ts
import { AdMob, AdOptions, AdSize, AdPosition } from '@capacitor-community/admob';

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss'],
})
export class AdMobComponent {
  private options: AdOptions = {
    adId: 'YOUR ADID',
    adSize: AdSize.BANNER,
    position: AdPosition.BOTTOM_CENTER,
    margin: 0,
    // isTesting: true
    // npa: true
  };

  constructor() {
    // Subscribe Banner Event Listener
    AdMob.addListener('bannerViewDidReceiveAd', (info: boolean) => {
      console.log('Banner Ad Loaded');
    });

    // Get Banner Size
    AdMob.addListener('bannerViewChangeSize', (info: boolean) => {
      console.log(info);
    });

    // Show Banner Ad
    AdMob.showBanner(this.options);
  }
}
```

#### React
This is implements simple sample from https://github.com/DavidFrahm . Thanks!

```ts
import React from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet, isPlatform } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import Home from './pages/Home';

import { AdMob, AdOptions, AdSize, AdPosition } from '@capacitor-community/admob';

const App: React.FC = () => {
  AdMob.initialize();

  const adId = {
    ios: 'ios-value-here',
    android: 'android-value-here',
  };

  const platformAdId = isPlatform('android') ? adId.android : adId.ios;

  const options: AdOptions = {
    adId: platformAdId,
    adSize: AdSize.BANNER,
    position: AdPosition.BOTTOM_CENTER,
    margin: 0,
    // isTesting: true
    // npa: true
  };

  AdMob.showBanner(options);

  // Subscribe Banner Event Listener
  AdMob.addListener('bannerViewDidReceiveAd', (info: boolean) => {
    console.log('Banner ad loaded');
  });

  // Get Banner Size
  AdMob.addListener('bannerViewChangeSize', (info: boolean) => {
    console.log(info);
  });

  return (
    <IonApp>
      <IonReactRouter>
        <IonRouterOutlet>
          <Route path="/home" component={Home} exact={true} />
          <Route exact path="/" render={() => <Redirect to="/home" />} />
        </IonRouterOutlet>
      </IonReactRouter>
    </IonApp>
  );
};

export default App;
```

### INTERSTITIAL

```ts
import { AdMob, AdOptions, AdLoadInfo, InterstitialAdPluginEvents } from '@capacitor-community/admob';

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss'],
})
export class AppComponent {
  options: AdOptions = {
    adId: 'YOUR ADID',
  };

  constructor() {
    // Subscribe interstitial Event Listener
    AdMob.addListener(InterstitialAdPluginEvents.Loaded, (info: AdLoadInfo) => {
      AdMob.showInterstitial();
    });

    // Prepare interstitial banner
    AdMob.prepareInterstitial(this.options);
  }
}
```

### RewardVideo

```ts
import { AdMob, AdOptions } from '@capacitor-community/admob';

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss'],
})
export class AdMobComponent {
  options: AdOptions = {
    adId: 'YOUR ADID',
  };

  constructor() {
    AdMob.addListener('adDidPresentFullScreenContent', () => {
      AdMob.showRewardVideoAd();
    });
    AdMob.prepareRewardVideoAd(this.options);
  }
}
```

## Index
<docgen-index>

* [`initialize(...)`](#initialize)
* [`showBanner(...)`](#showbanner)
* [`hideBanner()`](#hidebanner)
* [`resumeBanner()`](#resumebanner)
* [`removeBanner()`](#removebanner)
* [`addListener(BannerAdPluginEvents.SizeChanged, ...)`](#addlistenerbanneradplugineventssizechanged-)
* [`addListener(BannerAdPluginEvents.Loaded, ...)`](#addlistenerbanneradplugineventsloaded-)
* [`addListener(BannerAdPluginEvents.FailedToLoad, ...)`](#addlistenerbanneradplugineventsfailedtoload-)
* [`addListener(BannerAdPluginEvents.Opened, ...)`](#addlistenerbanneradplugineventsopened-)
* [`addListener(BannerAdPluginEvents.Closed, ...)`](#addlistenerbanneradplugineventsclosed-)
* [`addListener(BannerAdPluginEvents.AdImpression, ...)`](#addlistenerbanneradplugineventsadimpression-)
* [`prepareInterstitial(...)`](#prepareinterstitial)
* [`showInterstitial()`](#showinterstitial)
* [`addListener(InterstitialAdPluginEvents.FailedToLoad, ...)`](#addlistenerinterstitialadplugineventsfailedtoload-)
* [`addListener(InterstitialAdPluginEvents.Loaded, ...)`](#addlistenerinterstitialadplugineventsloaded-)
* [`addListener(InterstitialAdPluginEvents.Dismissed, ...)`](#addlistenerinterstitialadplugineventsdismissed-)
* [`addListener(InterstitialAdPluginEvents.FailedToShow, ...)`](#addlistenerinterstitialadplugineventsfailedtoshow-)
* [`addListener(InterstitialAdPluginEvents.Showed, ...)`](#addlistenerinterstitialadplugineventsshowed-)
* [`prepareRewardVideoAd(...)`](#preparerewardvideoad)
* [`showRewardVideoAd()`](#showrewardvideoad)
* [`addListener(RewardAdPluginEvents.FailedToLoad, ...)`](#addlistenerrewardadplugineventsfailedtoload-)
* [`addListener(RewardAdPluginEvents.Loaded, ...)`](#addlistenerrewardadplugineventsloaded-)
* [`addListener(RewardAdPluginEvents.Rewarded, ...)`](#addlistenerrewardadplugineventsrewarded-)
* [`addListener(RewardAdPluginEvents.Dismissed, ...)`](#addlistenerrewardadplugineventsdismissed-)
* [`addListener(RewardAdPluginEvents.FailedToShow, ...)`](#addlistenerrewardadplugineventsfailedtoshow-)
* [`addListener(RewardAdPluginEvents.Showed, ...)`](#addlistenerrewardadplugineventsshowed-)
* [Interfaces](#interfaces)
* [Enums](#enums)

</docgen-index>

## API
<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: AdMobInitializationOptions) => Promise<void>
```

Initialize AdMob with <a href="#admobinitializationoptions">AdMobInitializationOptions</a>

| Param         | Type                                                                              | Description                                                          |
| ------------- | --------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| **`options`** | <code><a href="#admobinitializationoptions">AdMobInitializationOptions</a></code> | <a href="#admobinitializationoptions">AdMobInitializationOptions</a> |

**Since:** 1.1.2

--------------------


### showBanner(...)

```typescript
showBanner(options: BannerAdOptions) => Promise<void>
```

Show a banner Ad

| Param         | Type                                                        | Description                        |
| ------------- | ----------------------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#banneradoptions">BannerAdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Since:** 1.1.2

--------------------


### hideBanner()

```typescript
hideBanner() => Promise<void>
```

Hide the banner, remove it from screen, but can show it later

**Since:** 1.1.2

--------------------


### resumeBanner()

```typescript
resumeBanner() => Promise<void>
```

Resume the banner, show it after hide

**Since:** 1.1.2

--------------------


### removeBanner()

```typescript
removeBanner() => Promise<void>
```

Destroy the banner, remove it from screen.

**Since:** 1.1.2

--------------------


### addListener(BannerAdPluginEvents.SizeChanged, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.SizeChanged, listenerFunc: (info: AdMobBannerSize) => void) => PluginListenerHandle
```

| Param              | Type                                                                              | Description         |
| ------------------ | --------------------------------------------------------------------------------- | ------------------- |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.SizeChanged</a></code> | bannerAdSizeChanged |
| **`listenerFunc`** | <code>(info: <a href="#admobbannersize">AdMobBannerSize</a>) =&gt; void</code>    |                     |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### addListener(BannerAdPluginEvents.Loaded, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.Loaded, listenerFunc: (info: AdMobBannerSize) => void) => PluginListenerHandle
```

Notice: request loaded Banner ad

| Param              | Type                                                                           | Description    |
| ------------------ | ------------------------------------------------------------------------------ | -------------- |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.Loaded</a></code>   | bannerAdLoaded |
| **`listenerFunc`** | <code>(info: <a href="#admobbannersize">AdMobBannerSize</a>) =&gt; void</code> |                |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### addListener(BannerAdPluginEvents.FailedToLoad, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.FailedToLoad, listenerFunc: (info: AdMobError) => void) => PluginListenerHandle
```

Notice: request failed Banner ad

| Param              | Type                                                                               | Description          |
| ------------------ | ---------------------------------------------------------------------------------- | -------------------- |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.FailedToLoad</a></code> | bannerAdFailedToLoad |
| **`listenerFunc`** | <code>(info: <a href="#admoberror">AdMobError</a>) =&gt; void</code>               |                      |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### addListener(BannerAdPluginEvents.Opened, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.Opened, listenerFunc: () => void) => PluginListenerHandle
```

Notice: full-screen banner view will be presented in response to the user clicking on an ad.

| Param              | Type                                                                         | Description    |
| ------------------ | ---------------------------------------------------------------------------- | -------------- |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.Opened</a></code> | bannerAdOpened |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                   |                |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### addListener(BannerAdPluginEvents.Closed, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.Closed, listenerFunc: () => void) => PluginListenerHandle
```

Notice: The full-screen banner view will been dismissed.

| Param              | Type                                                                         | Description    |
| ------------------ | ---------------------------------------------------------------------------- | -------------- |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.Closed</a></code> | bannerAdClosed |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                   |                |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### addListener(BannerAdPluginEvents.AdImpression, ...)

```typescript
addListener(eventName: BannerAdPluginEvents.AdImpression, listenerFunc: () => void) => PluginListenerHandle
```

Unimplemented

| Param              | Type                                                                               | Description  |
| ------------------ | ---------------------------------------------------------------------------------- | ------------ |
| **`eventName`**    | <code><a href="#banneradpluginevents">BannerAdPluginEvents.AdImpression</a></code> | AdImpression |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                         |              |

**Returns:** <code>PluginListenerHandle</code>

**Since:** 3.0.0

--------------------


### prepareInterstitial(...)

```typescript
prepareInterstitial(options: AdOptions) => Promise<AdLoadInfo>
```

Prepare interstitial banner

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Returns:** <code>Promise&lt;<a href="#adloadinfo">AdLoadInfo</a>&gt;</code>

**Since:** 1.1.2

--------------------


### showInterstitial()

```typescript
showInterstitial() => Promise<void>
```

Show interstitial ad when it’s ready

**Since:** 1.1.2

--------------------


### addListener(InterstitialAdPluginEvents.FailedToLoad, ...)

```typescript
addListener(eventName: InterstitialAdPluginEvents.FailedToLoad, listenerFunc: (error: AdMobError) => void) => PluginListenerHandle
```

| Param              | Type                                                                                           |
| ------------------ | ---------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#interstitialadpluginevents">InterstitialAdPluginEvents.FailedToLoad</a></code> |
| **`listenerFunc`** | <code>(error: <a href="#admoberror">AdMobError</a>) =&gt; void</code>                          |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(InterstitialAdPluginEvents.Loaded, ...)

```typescript
addListener(eventName: InterstitialAdPluginEvents.Loaded, listenerFunc: (info: AdLoadInfo) => void) => PluginListenerHandle
```

| Param              | Type                                                                                     |
| ------------------ | ---------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#interstitialadpluginevents">InterstitialAdPluginEvents.Loaded</a></code> |
| **`listenerFunc`** | <code>(info: <a href="#adloadinfo">AdLoadInfo</a>) =&gt; void</code>                     |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(InterstitialAdPluginEvents.Dismissed, ...)

```typescript
addListener(eventName: InterstitialAdPluginEvents.Dismissed, listenerFunc: () => void) => PluginListenerHandle
```

| Param              | Type                                                                                        |
| ------------------ | ------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#interstitialadpluginevents">InterstitialAdPluginEvents.Dismissed</a></code> |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                                  |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(InterstitialAdPluginEvents.FailedToShow, ...)

```typescript
addListener(eventName: InterstitialAdPluginEvents.FailedToShow, listenerFunc: (error: AdMobError) => void) => PluginListenerHandle
```

| Param              | Type                                                                                           |
| ------------------ | ---------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#interstitialadpluginevents">InterstitialAdPluginEvents.FailedToShow</a></code> |
| **`listenerFunc`** | <code>(error: <a href="#admoberror">AdMobError</a>) =&gt; void</code>                          |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(InterstitialAdPluginEvents.Showed, ...)

```typescript
addListener(eventName: InterstitialAdPluginEvents.Showed, listenerFunc: () => void) => PluginListenerHandle
```

| Param              | Type                                                                                     |
| ------------------ | ---------------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#interstitialadpluginevents">InterstitialAdPluginEvents.Showed</a></code> |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                               |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### prepareRewardVideoAd(...)

```typescript
prepareRewardVideoAd(options: AdOptions) => Promise<AdLoadInfo>
```

Prepare a reward video ad

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Returns:** <code>Promise&lt;<a href="#adloadinfo">AdLoadInfo</a>&gt;</code>

**Since:** 1.1.2

--------------------


### showRewardVideoAd()

```typescript
showRewardVideoAd() => Promise<AdMobRewardItem>
```

Show a reward video ad

**Returns:** <code>Promise&lt;<a href="#admobrewarditem">AdMobRewardItem</a>&gt;</code>

**Since:** 1.1.2

--------------------


### addListener(RewardAdPluginEvents.FailedToLoad, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.FailedToLoad, listenerFunc: (error: AdMobError) => void) => PluginListenerHandle
```

| Param              | Type                                                                               |
| ------------------ | ---------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.FailedToLoad</a></code> |
| **`listenerFunc`** | <code>(error: <a href="#admoberror">AdMobError</a>) =&gt; void</code>              |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(RewardAdPluginEvents.Loaded, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.Loaded, listenerFunc: (info: AdLoadInfo) => void) => PluginListenerHandle
```

| Param              | Type                                                                         |
| ------------------ | ---------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.Loaded</a></code> |
| **`listenerFunc`** | <code>(info: <a href="#adloadinfo">AdLoadInfo</a>) =&gt; void</code>         |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(RewardAdPluginEvents.Rewarded, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.Rewarded, listenerFunc: (reward: AdMobRewardItem) => void) => PluginListenerHandle
```

| Param              | Type                                                                             |
| ------------------ | -------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.Rewarded</a></code>   |
| **`listenerFunc`** | <code>(reward: <a href="#admobrewarditem">AdMobRewardItem</a>) =&gt; void</code> |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(RewardAdPluginEvents.Dismissed, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.Dismissed, listenerFunc: () => void) => PluginListenerHandle
```

| Param              | Type                                                                            |
| ------------------ | ------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.Dismissed</a></code> |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                      |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(RewardAdPluginEvents.FailedToShow, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.FailedToShow, listenerFunc: (error: AdMobError) => void) => PluginListenerHandle
```

| Param              | Type                                                                               |
| ------------------ | ---------------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.FailedToShow</a></code> |
| **`listenerFunc`** | <code>(error: <a href="#admoberror">AdMobError</a>) =&gt; void</code>              |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### addListener(RewardAdPluginEvents.Showed, ...)

```typescript
addListener(eventName: RewardAdPluginEvents.Showed, listenerFunc: () => void) => PluginListenerHandle
```

| Param              | Type                                                                         |
| ------------------ | ---------------------------------------------------------------------------- |
| **`eventName`**    | <code><a href="#rewardadpluginevents">RewardAdPluginEvents.Showed</a></code> |
| **`listenerFunc`** | <code>() =&gt; void</code>                                                   |

**Returns:** <code>PluginListenerHandle</code>

--------------------


### Interfaces


#### AdMobInitializationOptions

| Prop                               | Type                  | Description                                                                                                                                                                                                                                                | Default            | Since |
| ---------------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`requestTrackingAuthorization`** | <code>boolean</code>  | Use or not requestTrackingAuthorization in iOS(&gt;14)                                                                                                                                                                                                     |                    | 1.1.2 |
| **`testingDevices`**               | <code>string[]</code> | An Array of devices IDs that will be marked as tested devices if {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.initializeForTesting</a>} is true (Real Ads will be served to Testing devices but they will not count as 'real'). |                    | 1.2.0 |
| **`initializeForTesting`**         | <code>boolean</code>  | If set to true, the devices on {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.testingDevices</a>} will be registered to receive test production ads.                                                                              | <code>false</code> | 1.2.0 |


#### BannerAdOptions

| Prop           | Type                                                          | Description                                                                                                                                  | Default                   | Since |
| -------------- | ------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- | ----- |
| **`adSize`**   | <code><a href="#banneradsize">BannerAdSize</a></code>         | Banner Ad Size, defaults to SMART_BANNER. IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM | <code>SMART_BANNER</code> | 1.1.2 |
| **`position`** | <code><a href="#banneradposition">BannerAdPosition</a></code> | Set Banner Ad position. TOP_CENTER or CENTER or BOTTOM_CENTER                                                                                | <code>TOP_CENTER</code>   | 1.1.2 |


#### AdMobBannerSize

When notice listener of OnAdLoaded, you can get banner size.

| Prop         | Type                |
| ------------ | ------------------- |
| **`width`**  | <code>number</code> |
| **`height`** | <code>number</code> |


#### AdMobError

For more information
https://developers.google.com/android/reference/com/google/android/gms/ads/AdError

| Prop          | Type                | Description                            |
| ------------- | ------------------- | -------------------------------------- |
| **`code`**    | <code>number</code> | Gets the error's code.                 |
| **`message`** | <code>string</code> | Gets the message describing the error. |


#### AdLoadInfo

| Prop           | Type                |
| -------------- | ------------------- |
| **`adUnitId`** | <code>string</code> |


#### AdOptions

| Prop            | Type                 | Description                                                                                                                                  | Default            | Since |
| --------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`adId`**      | <code>string</code>  | The ad unit ID that you want to request                                                                                                      |                    | 1.1.2 |
| **`isTesting`** | <code>boolean</code> | You can use test mode of ad.                                                                                                                 | <code>false</code> | 1.1.2 |
| **`margin`**    | <code>number</code>  | Margin Banner. Default is 0px; If position is BOTTOM_CENTER, margin is be margin-bottom. If position is TOP_CENTER, margin is be margin-top. | <code>0</code>     | 1.1.2 |
| **`npa`**       | <code>boolean</code> | The default behavior of the Google Mobile Ads SDK is to serve personalized ads. Set this to true to request Non-Personalized Ads             | <code>false</code> | 1.2.0 |


#### AdMobRewardItem

For more information
https://developers.google.com/admob/android/rewarded-video-adapters?hl=en

| Prop         | Type                | Description              |
| ------------ | ------------------- | ------------------------ |
| **`type`**   | <code>string</code> | Rewarded type user got   |
| **`amount`** | <code>number</code> | Rewarded amount user got |


### Enums


#### BannerAdSize

| Members                | Value                           | Description                                                                                         |
| ---------------------- | ------------------------------- | --------------------------------------------------------------------------------------------------- |
| **`BANNER`**           | <code>'BANNER'</code>           | Mobile Marketing Association (MMA) banner ad size (320x50 density-independent pixels).              |
| **`FULL_BANNER`**      | <code>'FULL_BANNER'</code>      | Interactive Advertising Bureau (IAB) full banner ad size (468x60 density-independent pixels).       |
| **`LARGE_BANNER`**     | <code>'LARGE_BANNER'</code>     | Large banner ad size (320x100 density-independent pixels).                                          |
| **`LEADERBOARD`**      | <code>'LEADERBOARD'</code>      | Interactive Advertising Bureau (IAB) leaderboard ad size (728x90 density-independent pixels).       |
| **`MEDIUM_RECTANGLE`** | <code>'MEDIUM_RECTANGLE'</code> | Interactive Advertising Bureau (IAB) medium rectangle ad size (300x250 density-independent pixels). |
| **`ADAPTIVE_BANNER`**  | <code>'ADAPTIVE_BANNER'</code>  | A dynamically sized banner that is full-width and auto-height.                                      |
| **`SMART_BANNER`**     | <code>'SMART_BANNER'</code>     | Screen width x 32\|50\|90                                                                           |


#### BannerAdPosition

| Members             | Value                        | Description                               |
| ------------------- | ---------------------------- | ----------------------------------------- |
| **`TOP_CENTER`**    | <code>'TOP_CENTER'</code>    | Banner position be top-center             |
| **`CENTER`**        | <code>'CENTER'</code>        | Banner position be center                 |
| **`BOTTOM_CENTER`** | <code>'BOTTOM_CENTER'</code> | Banner position be bottom-center(default) |


#### BannerAdPluginEvents

| Members            | Value                               | Description                                                                                            |
| ------------------ | ----------------------------------- | ------------------------------------------------------------------------------------------------------ |
| **`SizeChanged`**  | <code>"bannerAdSizeChanged"</code>  |                                                                                                        |
| **`Loaded`**       | <code>"bannerAdLoaded"</code>       |                                                                                                        |
| **`FailedToLoad`** | <code>"bannerAdFailedToLoad"</code> |                                                                                                        |
| **`Opened`**       | <code>"bannerAdOpened"</code>       | Open or close "Adsense" Event after user click banner                                                  |
| **`Closed`**       | <code>"bannerAdClosed"</code>       |                                                                                                        |
| **`AdImpression`** | <code>"bannerAdImpression"</code>   | Similarly, this method should be called when an impression is recorded for the ad by the mediated SDK. |


#### InterstitialAdPluginEvents

| Members            | Value                                     | Description                                                                            |
| ------------------ | ----------------------------------------- | -------------------------------------------------------------------------------------- |
| **`Loaded`**       | <code>'interstitialAdLoaded'</code>       | Emits after trying to prepare and Interstitial, when it is loaded and ready to be show |
| **`FailedToLoad`** | <code>'interstitialAdFailedToLoad'</code> | Emits after trying to prepare and Interstitial, when it could not be loaded            |
| **`Showed`**       | <code>'interstitialAdShowed'</code>       | Emits when the Interstitial ad is visible to the user                                  |
| **`FailedToShow`** | <code>'interstitialAdFailedToShow'</code> |                                                                                        |
| **`Dismissed`**    | <code>'interstitialAdDismissed'</code>    | Emits when the Interstitial ad is not visible to the user anymore.                     |


#### RewardAdPluginEvents

| Members            | Value                                        | Description                                                                                                                                                                                                                                                                                                                                            |
| ------------------ | -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`Loaded`**       | <code>'onRewardedVideoAdLoaded'</code>       | Emits after trying to prepare a RewardAd and the Video is loaded and ready to be show                                                                                                                                                                                                                                                                  |
| **`FailedToLoad`** | <code>'onRewardedVideoAdFailedToLoad'</code> | Emits after trying to prepare a RewardAd when it could not be loaded                                                                                                                                                                                                                                                                                   |
| **`Showed`**       | <code>'onRewardedVideoAdShowed'</code>       | Emits when the AdReward video is visible to the user                                                                                                                                                                                                                                                                                                   |
| **`FailedToShow`** | <code>'onRewardedVideoAdFailedToShow'</code> |                                                                                                                                                                                                                                                                                                                                                        |
| **`Dismissed`**    | <code>'onRewardedVideoAdDismissed'</code>    | Emits when the AdReward video is not visible to the user anymore. **Important**: This has nothing to do with the reward it self. This event will emits in this two cases: 1. The user starts the video ad but close it before the reward emit. 2. The user start the video and see it until end, then gets the reward and after that the ad is closed. |
| **`Rewarded`**     | <code>'onRewardedVideoAdReward'</code>       |                                                                                                                                                                                                                                                                                                                                                        |

</docgen-api>

## TROUBLE SHOOTING

### If you have error:

> [error] Error running update: Analyzing dependencies
> [!] CocoaPods could not find compatible versions for pod "Google-Mobile-Ads-SDK":

You should run `pod repo update` ;

## License

Capacitor AdMob is [MIT licensed](./LICENSE).

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://wako.app"><img src="https://avatars1.githubusercontent.com/u/216573?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Jean-Baptiste Malatrasi</b></sub></a><br /><a href="https://github.com/capacitor-community/admob/commits?author=JumBay" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/gant02"><img src="https://avatars1.githubusercontent.com/u/6771123?v=4?s=100" width="100px;" alt=""/><br /><sub><b>gant02</b></sub></a><br /><a href="https://github.com/capacitor-community/admob/commits?author=gant02" title="Code">💻</a></td>
    <td align="center"><a href="https://www.saninnsalas.com"><img src="https://avatars1.githubusercontent.com/u/5490201?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Saninn Salas Diaz</b></sub></a><br /><a href="https://github.com/capacitor-community/admob/commits?author=distante" title="Code">💻</a></td>
    <td align="center"><a href="https://www.nicolueg.com"><img src="https://avatars.githubusercontent.com/u/48101693?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Nico Lueg</b></sub></a><br /><a href="https://github.com/capacitor-community/admob/commits?author=NLueg" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
