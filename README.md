# [Capacitor AdMob](https://github.com/rdlabo/capacitor-admob) 💰💰💰

Capacitor AdMob is a native AdMob  implementation for IOS & Android. Now you can use this package as a [Ionic Capacitor](https://capacitor.ionicframework.com) Plugin in your App.

## Installation

```
$ npm install --save @rdlabo/capacitor-admob
```


## Android


### 📌 Update Manifest
Open your __app/src/Android/AndroidManifest.xml__ file and add this `meta-data` line at the right spot (and replace the value by the actual App ID of your app!):

```xml
<application>
  <!-- this line needs to be added (replace the value!) -->
  <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-6564742920318187~3878397693" />
  <activity></activity>
</application>

```

### 📌 Register AdMob to Capacitor

Open your Ionic Capacitor App in Android Studio, Now open __app/src/main/java/**/**/**/MainActivity.java__ of your app and Register AdMob to Capacitor Plugins.


```java
// Other imports...
public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      
      add(jp.rdlabo.capacitor.plugin.admob.AdMob.class);

    }});
  }
}
```

## iOS

### 📌 Update Info.plist
Open your __apps/ios/App/App/Info.plist__ file and add this `GADApplicationIdentifier` line at the right spot (and replace the value by the actual App ID of your app!):


```apps/ios/App/App/Info.plist
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-6564742920318187~7217030993</string>
```

### 📌 Register AdMob to Capacitor

Open your Ionic Capacitor App in Xcode, Now open __apps/ios/App/App/AppDelegate.swift__  of your app and Register AdMob to Capacitor Plugins.

```
import UIKit
import Capacitor
import GoogleMobileAds

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?


  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    GADMobileAds.sharedInstance().start(completionHandler: nil)
```

## 📌 Initialize AdMob

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

## 📌 BANNER

### showBanner(options: AdOptions): Promise<{ value: boolean }>

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


### hideBanner(): Promise<{ value: boolean }>

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


### resumeBanner(): Promise<{ value: boolean }>

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

### removeBanner(): Promise<{ value: boolean }>

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

### Event Listener
This following Event Listener can be called in **Banner AD**.
```ts
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void): PluginListenerHandle;
```



## 📌 INTERSTITIAL

### prepareInterstitial(options: AdOptions): Promise<{ value: boolean }>
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


### showInterstitial(): Promise<{ value: boolean }>

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

### Event Listener
This following Event Listener can be called in **Interstitial AD**
```ts
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void): PluginListenerHandle;

addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void): PluginListenerHandle;
```


## 📌 RewardVideo

### prepareRewardVideoAd(options: AdOptions): Promise<{ value: boolean }>

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


### showRewardVideoAd(): Promise<{ value: boolean }>

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


### pauseRewardedVideo(): Promise<{ value: boolean }>

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


### resumeRewardedVideo(): Promise<{ value: boolean }>

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

### stopRewardedVideo(): Promise<{ value: boolean }>

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

### Event Listener
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

# API 

### 📌 AdOptions
```ts
interface AdOptions {
  adId: string;
  adSize?: AdSize;
  position?: AdPosition;
}
```

### 📌 AdSize
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

### 📌 AdPosition
```ts
enum AdPosition {
    
  TOP_CENTER = 'TOP_CENTER',

  CENTER = 'CENTER',

  BOTTOM_CENTER = 'BOTTOM_CENTER',
}
```


## Contributing

- 🌟 Star this repository
- 📋 Open issue for feature requests


## Roadmap
 - [Capacitor Plugins](https://capacitor.ionicframework.com/docs/plugins/)

 - [IOS](https://capacitor.ionicframework.com/docs/plugins/ios/)

 - [Android](https://capacitor.ionicframework.com/docs/plugins/android/)


## License

Capacitor AdMob is [MIT licensed](./LICENSE).
