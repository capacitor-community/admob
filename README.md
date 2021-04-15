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
    AdMob.addListener('adDidPresentFullScreenContent', (info: boolean) => {
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
* [Interfaces](#interfaces)

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


### Interfaces


#### AdMobInitializationOptions

| Prop                               | Type                  | Description                                                                                                                                                                                                                                                | Default            | Since |
| ---------------------------------- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | ----- |
| **`requestTrackingAuthorization`** | <code>boolean</code>  | Use or not requestTrackingAuthorization in iOS(&gt;14)                                                                                                                                                                                                     |                    | 1.1.2 |
| **`testingDevices`**               | <code>string[]</code> | An Array of devices IDs that will be marked as tested devices if {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.initializeForTesting</a>} is true (Real Ads will be served to Testing devices but they will not count as 'real'). |                    | 1.2.0 |
| **`initializeForTesting`**         | <code>boolean</code>  | If set to true, the devices on {@link <a href="#admobinitializationoptions">AdMobInitializationOptions.testingDevices</a>} will be registered to receive test production ads.                                                                              | <code>false</code> | 1.2.0 |

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
