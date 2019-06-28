[![npm version](https://badge.fury.io/js/%40rdlabo%2Fcapacitor-admob.svg)](https://badge.fury.io/js/%40rdlabo%2Fcapacitor-admob)

# capacitor-admib

Capacitor AdMob is a native AdMob implementation for iOS & Android. 
This repository fork from `@rahadur/capacitor-admob` .

## Installation

```
$ npm install --save @rdlabo/capacitor-admob
```


## Android configuration

In file `android/app/src/main/java/**/**/MainActivity.java`, add the plugin to the initialization list:

```java
this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
  [...]
  add(jp.rdlabo.capacitor.plugin.admob.AdMob.class);
  [...]
}});
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

Don't forget to replace `[APP_ID]` by your Facebook application Id.


## iOS configuration

In file `ios/App/App/AppDelegate.swift` add or replace the following:

```
import GoogleMobileAds

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?


  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    GADMobileAds.sharedInstance().start(completionHandler: nil)
```

Add the following in the `ios/App/App/info.plist` file:

```xml
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-6564742920318187~7217030993</string>
```


## Initialize

Open our Ionic app __app.component.ts__ file and add this folloing code.

```ts
import { Plugins } from '@capacitor/core';
import { initialize } from '@rdlabo/capacitor-admob';

const { AdMob } = Plugins;

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent {
    constructor(){
        // Initialize AdMob for your Application
        AdMob.initialize('YOUR APPID');
    }
}
```

## APIs

### 📌 BANNER

#### showBanner(options: AdOptions): Promise<{ value: boolean }>

```ts
import { Plugins } from '@capacitor/core';
import { AdOptions, AdSize, AdPosition } from '@rdlabo/capacitor-admob';

const { AdMob } = Plugins;

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss']
})
export class AdMobComponent {

    const options: AdOptions = {
        adId: 'YOUR ADID',
        adSize: AdSize.Banner,
        position: AdPosition.BOTTOM_CENTER
    }

    constructor(){
        // Show Banner Ad
        AdMob.showBanner(this.options)
        .then(
            (value) => {
                console.log(value);  // true
            },
            (error) => {
                console.err(error); // show error
            } 
        );


        // Subscibe Banner Event Listener
        AdMob.addListener('onAdLoaded', (info: boolean) => {
             console.log("Banner Ad Loaded");
        });
    }
}
```


#### hideBanner(): Promise<{ value: boolean }>

```ts
// Hide the banner, remove it from screen, but can show it later

AdMob.hideBanner().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```


#### resumeBanner(): Promise<{ value: boolean }>

```ts
// Resume the banner, show it after hide

AdMob.resumeBanner().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```

#### removeBanner(): Promise<{ value: boolean }>

```ts
// Destroy the banner, remove it from screen.

AdMob.removeBanner().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```

#### Event Listener
This following Event Listener can be called in **Banner AD**.
```ts
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void): PluginListenerHandle;
```


### INTERSTITIAL

#### prepareInterstitial(options: AdOptions): Promise<{ value: boolean }>
```ts
import { Plugins } from '@capacitor/core';
import { AdOptions } from '@rdlabo/capacitor-admob';

const { AdMob } = Plugins;

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss']
})
export class AppComponent {

    const options: AdOptions = {
        adId: 'YOUR ADID',
        autoShow: false
    }

    constructor(){

        // Prepare interstitial banner
        AdMob.prepareInterstitial(this.options)
        .then(
            (value) => {
                console.log(value);  // true
            },
            (error) => {
                console.err(error); // show error
            } 
        );


        // Subscibe Banner Event Listener
        AdMob.addListener('onAdLoaded', (info: boolean) => {
            
            // You can call showInterstitial() here or anytime you want.

             console.log("Interstitial Ad Loaded");
        });
    }
}
```


#### showInterstitial(): Promise<{ value: boolean }>

```ts
// Show interstitial ad when it’s ready

AdMob.showInterstitial().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```

#### Event Listener
This following Event Listener can be called in **Interstitial AD**
```ts
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void): PluginListenerHandle;
```


### RewardVideo

#### prepareRewardVideoAd(options: AdOptions): Promise<{ value: boolean }>

```ts
import { Plugins } from '@capacitor/core';
import { AdOptions } from '@rdlabo/capacitor-admob';

const { AdMob } = Plugins;

@Component({
  selector: 'admob',
  templateUrl: 'admob.component.html',
  styleUrls: ['admob.component.scss']
})
export class AAdMobComponent {

    const options: AdOptions = {
        adId: 'YOUR ADID'
    }

    constructor(){

        // Prepare ReWardVideo
        AdMob.prepareRewardVideoAd(this.options)
        .then(
            (value) => {
                console.log(value);  // true
            },
            (error) => {
                console.err(error); // show error
            } 
        );


        // Subscibe ReWardVideo Event Listener
        AdMob.addListener('onRewardedVideoAdLoaded', (info: boolean) => {
            
            // You can call showRewardVideoAd() here or anytime you want.

             console.log("RewardedVideoAd Loaded");
        });
    }
}
```


#### showRewardVideoAd(): Promise<{ value: boolean }>

```typescript
// Show a RewardVideo AD

AdMob.showRewardVideoAd().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```


#### pauseRewardedVideo(): Promise<{ value: boolean }>

```ts
// Pause a RewardVideo AD

AdMob.pauseRewardedVideo().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```


#### resumeRewardedVideo(): Promise<{ value: boolean }>

```ts
// Resume a RewardVideo AD

AdMob.resumeRewardedVideo().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```

#### stopRewardedVideo(): Promise<{ value: boolean }>

```ts
// Stop a RewardVideo AD

AdMob.stopRewardedVideo().then(
    (value) => {
        console.log(value);  // true
    },
    (error) => {
        console.err(error); // show error
    } 
);
```

#### Event Listener
This following Event Listener can be called in **RewardedVideo**
```ts
addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoStarted', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoAdClosed', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewarded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoAdLeftApplication', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onRewardedVideoCompleted', listenerFunc: (info: any) => void): PluginListenerHandle;
```

## Options

### AdOptions
```ts
interface AdOptions {
  adId: string;
  adSize?: AdSize;
  position?: AdPosition;
}
```

### AdSize
```ts
enum AdSize {
  BANNER = 'BANNER',

  FLUID = 'FLUID',

  FULL_BANNER = 'FULL_BANNER',

  LARGE_BANNER = 'LARGE_BANNER',

  LEADERBOARD = 'LEADERBOARD',

  MEDIUM_RECTANGLE = 'MEDIUM_RECTANGLE',

  SMART_BANNER = 'SMART_BANNER',

  CUSTOM = 'CUSTOM'
}
```

### AdPosition
```ts
enum AdPosition {
    
  TOP_CENTER = 'TOP_CENTER',

  CENTER = 'CENTER',

  BOTTOM_CENTER = 'BOTTOM_CENTER',
}
```


## License
Capacitor AdMob is [MIT licensed](./LICENSE).
