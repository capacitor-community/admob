[![npm version](https://badge.fury.io/js/%40capacitor-community%2Fadmob.svg)](https://badge.fury.io/js/%40capacitor-community%2Fadmob)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

# ✅ Please check
This is development README for Capacitor v3. If you use v1 or v2, please check [./README_v2.md](README_v2.md)

# @capacitor-community/admob

Capacitory community plugin for AdMob.

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

```
initialize(options: { requestTrackingAuthorization?: boolean , testingDevices?: string[]}): Promise<{ value: boolean }>
```

You can use option `requestTrackingAuthorization`. This change permission to require `AppTrackingTransparency` in iOS >= 14:
https://developers.google.com/admob/ios/ios14

Default value is `true`. If you don't want to track, set requestTrackingAuthorization `false`.

Send and array of device Ids in `testingDevices? to use production like ads on your specified devices -> https://developers.google.com/admob/android/test-ads#enable_test_devices

### Initialize for @ionic/angular

Open our Ionic app **app.component.ts** file and add this following code.

```ts
import { AdMob } from '@capacitor-community/admob';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  constructor() {
    // Initialize AdMob for your Application
    AdMob.initialize();
  }
}
```

### Initialize for @ionic/react

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
  AdMob.addListener('onAdLoaded', (info: boolean) => {
    console.log('Banner ad loaded');
  });

  // Get Banner Size
  AdMob.addListener('onAdSize', (info: boolean) => {
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

## APIs

### BANNER

#### showBanner(options: AdOptions): Promise<{ value: boolean }>

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
    // Show Banner Ad
    AdMob.showBanner(this.options);

    // Subscribe Banner Event Listener
    AdMob.addListener('bannerViewDidReceiveAd', (info: boolean) => {
      console.log('Banner Ad Loaded');
    });

    // Get Banner Size
    AdMob.addListener('bannerViewReceiveAdSize', (info: boolean) => {
      console.log(info);
    });
  }
}
```

#### hideBanner(): Promise<{ value: boolean }>

```ts
// Hide the banner, remove it from screen, but can show it later
AdMob.hideBanner();
```

#### resumeBanner(): Promise<{ value: boolean }>

```ts
// Resume the banner, show it after hide
AdMob.resumeBanner();
```

#### removeBanner(): Promise<{ value: boolean }>

```ts
// Destroy the banner, remove it from screen.
AdMob.removeBanner();
```

### INTERSTITIAL

```ts
import { AdMob, AdOptions } from '@capacitor-community/admob';

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
    AdMob.addListener('onInterstitialAdLoaded', (info: boolean) => {
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
    AdMob.addListener('onRewardedVideoAdLoaded', () => {
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
* [`prepareInterstitial(...)`](#prepareinterstitial)
* [`showInterstitial()`](#showinterstitial)
* [`prepareRewardVideoAd(...)`](#preparerewardvideoad)
* [`showRewardVideoAd()`](#showrewardvideoad)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
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
showBanner(options: AdOptions) => Promise<void>
```

Show a banner Ad

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

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


### prepareInterstitial(...)

```typescript
prepareInterstitial(options: AdOptions) => Promise<void>
```

Prepare interstitial banner

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Since:** 1.1.2

--------------------


### showInterstitial()

```typescript
showInterstitial() => Promise<void>
```

Show interstitial ad when it’s ready

**Since:** 1.1.2

--------------------


### prepareRewardVideoAd(...)

```typescript
prepareRewardVideoAd(options: AdOptions) => Promise<void>
```

Prepare a reward video ad

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

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


### addListener(...)

```typescript
addListener(eventName: 'bannerViewReceiveAdSize', listenerFunc: (info: AdMobBannerSize) => void) => PluginListenerHandle
```

| Param              | Type                                                                           | Description             |
| ------------------ | ------------------------------------------------------------------------------ | ----------------------- |
| **`eventName`**    | <code>"bannerViewReceiveAdSize"</code>                                         | bannerViewReceiveAdSize |
| **`listenerFunc`** | <code>(info: <a href="#admobbannersize">AdMobBannerSize</a>) =&gt; void</code> |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerViewDidReceiveAd', listenerFunc: () => void) => PluginListenerHandle
```

Notice: request loaded Banner ad

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"bannerViewDidReceiveAd"</code> | bannerViewDidReceiveAd |
| **`listenerFunc`** | <code>() =&gt; void</code>            |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerView:didFailToReceiveAdWithError', listenerFunc: (info: AdMobError) => void) => PluginListenerHandle
```

Notice: request failed Banner ad

| Param              | Type                                                                 | Description                            |
| ------------------ | -------------------------------------------------------------------- | -------------------------------------- |
| **`eventName`**    | <code>"bannerView:didFailToReceiveAdWithError"</code>                | bannerView:didFailToReceiveAdWithError |
| **`listenerFunc`** | <code>(info: <a href="#admoberror">AdMobError</a>) =&gt; void</code> |                                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerViewWillPresentScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: full-screen banner view will be presented in response to the user clicking on an ad.

| Param              | Type                                       | Description                 |
| ------------------ | ------------------------------------------ | --------------------------- |
| **`eventName`**    | <code>"bannerViewWillPresentScreen"</code> | bannerViewWillPresentScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>        |                             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerViewWillDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen banner view will be dismissed.

| Param              | Type                                       | Description                 |
| ------------------ | ------------------------------------------ | --------------------------- |
| **`eventName`**    | <code>"bannerViewWillDismissScreen"</code> | bannerViewWillDismissScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>        |                             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerViewWillDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen banner view will been dismissed.

| Param              | Type                                       | Description                 |
| ------------------ | ------------------------------------------ | --------------------------- |
| **`eventName`**    | <code>"bannerViewWillDismissScreen"</code> | bannerViewWillDismissScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>        |                             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'bannerViewDidDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen banner view has been dismissed.

| Param              | Type                                      | Description                |
| ------------------ | ----------------------------------------- | -------------------------- |
| **`eventName`**    | <code>"bannerViewDidDismissScreen"</code> | bannerViewDidDismissScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>       |                            |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdLoaded', listenerFunc: () => void) => PluginListenerHandle
```

Notice: Prepared InterstitialAd

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"onInterstitialAdLoaded"</code> | onInterstitialAdLoaded |
| **`listenerFunc`** | <code>() =&gt; void</code>            |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: () => void) => PluginListenerHandle
```

Notice: Prepared RewardedVideo

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdLoaded"</code> | onRewardedVideoAdLoaded |
| **`listenerFunc`** | <code>() =&gt; void</code>             |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adDidPresentFullScreenContent', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad opened

| Param              | Type                                         | Description                   |
| ------------------ | -------------------------------------------- | ----------------------------- |
| **`eventName`**    | <code>"adDidPresentFullScreenContent"</code> | adDidPresentFullScreenContent |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>          |                               |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adDidDismissFullScreenContent', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Dismiss Content

| Param              | Type                                         | Description                   |
| ------------------ | -------------------------------------------- | ----------------------------- |
| **`eventName`**    | <code>"adDidDismissFullScreenContent"</code> | adDidDismissFullScreenContent |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>          |                               |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 3.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'didFailToPresentFullScreenContentWithError', listenerFunc: (info: AdMobError) => void) => PluginListenerHandle
```

Notice: Interstitial ad is be failed to open

| Param              | Type                                                                 | Description                                |
| ------------------ | -------------------------------------------------------------------- | ------------------------------------------ |
| **`eventName`**    | <code>"didFailToPresentFullScreenContentWithError"</code>            | didFailToPresentFullScreenContentWithError |
| **`listenerFunc`** | <code>(info: <a href="#admoberror">AdMobError</a>) =&gt; void</code> |                                            |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.2.0

--------------------


### Interfaces


#### AdMobInitializationOptions

| Prop                               | Type                  | Description                                                                                                                                                                                                                                                | Default            | Since |
| ---------------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`requestTrackingAuthorization`** | <code>boolean</code>  | Use or not requestTrackingAuthorization in iOS(&gt;14)                                                                                                                                                                                                     |                    | 1.1.2 |
| **`testingDevices`**               | <code>string[]</code> | An Array of devices IDs that will be marked as tested devices if {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.initializeForTesting</a>} is true (Real Ads will be served to Testing devices but they will not count as 'real'). |                    | 1.2.0 |
| **`initializeForTesting`**         | <code>boolean</code>  | If set to true, the devices on {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.testingDevices</a>} will be registered to receive test production ads.                                                                              | <code>false</code> | 1.2.0 |


#### AdOptions

| Prop            | Type                                              | Description                                                                                                                                  | Default                   | Since |
| --------------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- | ----- |
| **`adId`**      | <code>string</code>                               | The ad unit ID that you want to request                                                                                                      |                           | 1.1.2 |
| **`adSize`**    | <code><a href="#adsize">AdSize</a></code>         | Banner Ad Size, defaults to SMART_BANNER. IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM | <code>SMART_BANNER</code> | 1.1.2 |
| **`position`**  | <code><a href="#adposition">AdPosition</a></code> | Set Banner Ad position. TOP_CENTER or CENTER or BOTTOM_CENTER                                                                                | <code>TOP_CENTER</code>   | 1.1.2 |
| **`isTesting`** | <code>boolean</code>                              | You can use test mode of ad.                                                                                                                 | <code>false</code>        | 1.1.2 |
| **`margin`**    | <code>number</code>                               | Margin Banner. Default is 0px; If position is BOTTOM_CENTER, margin is be margin-bottom. If position is TOP_CENTER, margin is be margin-top. | <code>0</code>            | 1.1.2 |
| **`npa`**       | <code>boolean</code>                              | The default behavior of the Google Mobile Ads SDK is to serve personalized ads. Set this to true to request Non-Personalized Ads             | <code>false</code>        | 1.2.0 |


#### AdMobRewardItem

For more information
https://developers.google.com/admob/android/rewarded-video-adapters?hl=en

| Prop         | Type                | Description              |
| ------------ | ------------------- | ------------------------ |
| **`type`**   | <code>string</code> | Rewarded type user got   |
| **`amount`** | <code>number</code> | Rewarded amount user got |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### AdMobBannerSize

When notice listener of OnAdLoaded, you can get banner size.

| Prop         | Type                |
| ------------ | ------------------- |
| **`width`**  | <code>number</code> |
| **`height`** | <code>number</code> |


#### AdMobError

For more information
https://developers.google.com/android/reference/com/google/android/gms/ads/AdError

| Prop          | Type                | Description                                                                                                                                            |
| ------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`code`**    | <code>number</code> | Gets the error's code.                                                                                                                                 |
| **`message`** | <code>string</code> | Gets the message describing the error.                                                                                                                 |
| **`cause`**   | <code>string</code> | Gets the cause of this error or null if the cause is nonexistent or unknown.                                                                           |
| **`domain`**  | <code>string</code> | Gets the domain of the error. MobileAds.ERROR_DOMAIN for Google Mobile Ads SDK errors, or a domain defined by mediation networks for mediation errors. |


### Enums


#### AdSize

| Members                | Value                           | Description                                                                                                                                    |
| ---------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| **`BANNER`**           | <code>'BANNER'</code>           | Mobile Marketing Association (MMA) banner ad size (320x50 density-independent pixels).                                                         |
| **`FLUID`**            | <code>'FLUID'</code>            | A dynamically sized banner that matches its parent's width and expands/contracts its height to match the ad's content after loading completes. |
| **`FULL_BANNER`**      | <code>'FULL_BANNER'</code>      | Interactive Advertising Bureau (IAB) full banner ad size (468x60 density-independent pixels).                                                  |
| **`LARGE_BANNER`**     | <code>'LARGE_BANNER'</code>     | Large banner ad size (320x100 density-independent pixels).                                                                                     |
| **`LEADERBOARD`**      | <code>'LEADERBOARD'</code>      | Interactive Advertising Bureau (IAB) leaderboard ad size (728x90 density-independent pixels).                                                  |
| **`MEDIUM_RECTANGLE`** | <code>'MEDIUM_RECTANGLE'</code> | Interactive Advertising Bureau (IAB) medium rectangle ad size (300x250 density-independent pixels).                                            |
| **`SMART_BANNER`**     | <code>'SMART_BANNER'</code>     | deprecated: A dynamically sized banner that is full-width and auto-height.                                                                     |
| **`ADAPTIVE_BANNER`**  | <code>'ADAPTIVE_BANNER'</code>  | A dynamically sized banner that is full-width and auto-height.                                                                                 |
| **`CUSTOM`**           | <code>'CUSTOM'</code>           | To define a custom banner size, set your desired <a href="#adsize">AdSize</a>                                                                  |


#### AdPosition

| Members             | Value                        | Description                               |
| ------------------- | ---------------------------- | ----------------------------------------- |
| **`TOP_CENTER`**    | <code>'TOP_CENTER'</code>    | Banner position be top-center             |
| **`CENTER`**        | <code>'CENTER'</code>        | Banner position be center                 |
| **`BOTTOM_CENTER`** | <code>'BOTTOM_CENTER'</code> | Banner position be bottom-center(default) |

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
