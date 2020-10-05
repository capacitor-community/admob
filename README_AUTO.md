This API is created by https://www.npmjs.com/package/@capacitor/docgen
This README is developing test.

# DOCGEN_INDEX

<!--DOCGEN_INDEX_START-->
* [initialize()](#initialize)
* [showBanner()](#showbanner)
* [hideBanner()](#hidebanner)
* [resumeBanner()](#resumebanner)
* [removeBanner()](#removebanner)
* [prepareInterstitial()](#prepareinterstitial)
* [showInterstitial()](#showinterstitial)
* [prepareRewardVideoAd()](#preparerewardvideoad)
* [showRewardVideoAd()](#showrewardvideoad)
* [pauseRewardedVideo()](#pauserewardedvideo)
* [resumeRewardedVideo()](#resumerewardedvideo)
* [stopRewardedVideo()](#stoprewardedvideo)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [addListener()](#addlistener)
* [Interfaces](#interfaces)
* [Enums](#enums)
<!--DOCGEN_INDEX_END-->

## DOCGEN_API

<!--DOCGEN_API_START-->
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->
## API

### initialize

```typescript
initialize(options: AdMobInitializationOptions) => Promise<{ value: boolean; }>
```

Initialize AdMob with AdMobInitializationOptions

| Param       | Type                                                      | Description                |
| ----------- | --------------------------------------------------------- | -------------------------- |
| **options** | [AdMobInitializationOptions](#admobinitializationoptions) | AdMobInitializationOptions |

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### showBanner

```typescript
showBanner(options: AdOptions) => Promise<{ value: boolean; }>
```

Show a banner Ad

| Param       | Type                    | Description |
| ----------- | ----------------------- | ----------- |
| **options** | [AdOptions](#adoptions) | AdOptions   |

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### hideBanner

```typescript
hideBanner() => Promise<{ value: boolean; }>
```

Hide the banner, remove it from screen, but can show it later

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### resumeBanner

```typescript
resumeBanner() => Promise<{ value: boolean; }>
```

Resume the banner, show it after hide

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### removeBanner

```typescript
removeBanner() => Promise<{ value: boolean; }>
```

Destroy the banner, remove it from screen.

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### prepareInterstitial

```typescript
prepareInterstitial(options: AdOptions) => Promise<{ value: boolean; }>
```

Prepare interstitial banner

| Param       | Type                    | Description |
| ----------- | ----------------------- | ----------- |
| **options** | [AdOptions](#adoptions) | AdOptions   |

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### showInterstitial

```typescript
showInterstitial() => Promise<{ value: boolean; }>
```

Show interstitial ad when it’s ready

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### prepareRewardVideoAd

```typescript
prepareRewardVideoAd(options: AdOptions) => Promise<{ value: boolean; }>
```

Prepare a reward video ad

| Param       | Type                    | Description |
| ----------- | ----------------------- | ----------- |
| **options** | [AdOptions](#adoptions) | AdOptions   |

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### showRewardVideoAd

```typescript
showRewardVideoAd() => Promise<{ value: boolean; }>
```

Show a reward video ad

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### pauseRewardedVideo

```typescript
pauseRewardedVideo() => Promise<{ value: boolean; }>
```

Pause RewardedVideo

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### resumeRewardedVideo

```typescript
resumeRewardedVideo() => Promise<{ value: boolean; }>
```

Resume RewardedVideo

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### stopRewardedVideo

```typescript
stopRewardedVideo() => Promise<{ value: boolean; }>
```

Close RewardedVideo

**Returns:** Promise&lt;{ value: boolean; }&gt;

**Since:** 1.1.2

--------------------


### addListener

```typescript
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: banner ad is loaded(android)

| Param            | Type                | Description |
| ---------------- | ------------------- | ----------- |
| **eventName**    | "onAdLoaded"        | onAdLoaded  |
| **listenerFunc** | (info: any) => void |             |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: failed to load Banner ad(android)

| Param            | Type                | Description      |
| ---------------- | ------------------- | ---------------- |
| **eventName**    | "onAdFailedToLoad"  | onAdFailedToLoad |
| **listenerFunc** | (info: any) => void |                  |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: banner ad is show(android)

| Param            | Type                | Description |
| ---------------- | ------------------- | ----------- |
| **eventName**    | "onAdOpened"        | onAdOpened  |
| **listenerFunc** | (info: any) => void |             |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Banner ad is closed(android)

| Param            | Type                | Description |
| ---------------- | ------------------- | ----------- |
| **eventName**    | "onAdClosed"        | onAdClosed  |
| **listenerFunc** | (info: any) => void |             |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewDidReceiveAd', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: request loaded Banner ad(iOS)

| Param            | Type                 | Description        |
| ---------------- | -------------------- | ------------------ |
| **eventName**    | "adViewDidReceiveAd" | adViewDidReceiveAd |
| **listenerFunc** | (info: any) => void  |                    |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adView:didFailToReceiveAdWithError', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: request failed Banner ad(iOS)

| Param            | Type                                 | Description                        |
| ---------------- | ------------------------------------ | ---------------------------------- |
| **eventName**    | "adView:didFailToReceiveAdWithError" | adView:didFailToReceiveAdWithError |
| **listenerFunc** | (info: any) => void                  |                                    |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillPresentScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: full-screen view will be presented in response to the user clicking on an ad.(iOS)

| Param            | Type                      | Description             |
| ---------------- | ------------------------- | ----------------------- |
| **eventName**    | "adViewWillPresentScreen" | adViewWillPresentScreen |
| **listenerFunc** | (info: any) => void       |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen view will be dismissed.(iOS)

| Param            | Type                      | Description             |
| ---------------- | ------------------------- | ----------------------- |
| **eventName**    | "adViewWillDismissScreen" | adViewWillDismissScreen |
| **listenerFunc** | (info: any) => void       |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewDidDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen view has been dismissed.(iOS)

| Param            | Type                     | Description            |
| ---------------- | ------------------------ | ---------------------- |
| **eventName**    | "adViewDidDismissScreen" | adViewDidDismissScreen |
| **listenerFunc** | (info: any) => void      |                        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillLeaveApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: User click will open another app.(iOS)

| Param            | Type                         | Description                |
| ---------------- | ---------------------------- | -------------------------- |
| **eventName**    | "adViewWillLeaveApplication" | adViewWillLeaveApplication |
| **listenerFunc** | (info: any) => void          |                            |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onInterstitialAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad loaded

| Param            | Type                     | Description            |
| ---------------- | ------------------------ | ---------------------- |
| **eventName**    | "onInterstitialAdLoaded" | onInterstitialAdLoaded |
| **listenerFunc** | (info: any) => void      |                        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onInterstitialAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Failed to load Interstitial ad

| Param            | Type                           | Description                  |
| ---------------- | ------------------------------ | ---------------------------- |
| **eventName**    | "onInterstitialAdFailedToLoad" | onInterstitialAdFailedToLoad |
| **listenerFunc** | (info: any) => void            |                              |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onInterstitialAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad opened

| Param            | Type                     | Description            |
| ---------------- | ------------------------ | ---------------------- |
| **eventName**    | "onInterstitialAdOpened" | onInterstitialAdOpened |
| **listenerFunc** | (info: any) => void      |                        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onInterstitialAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad closed

| Param            | Type                     | Description            |
| ---------------- | ------------------------ | ---------------------- |
| **eventName**    | "onInterstitialAdClosed" | onInterstitialAdClosed |
| **listenerFunc** | (info: any) => void      |                        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onInterstitialAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Click link of Interstitial ad

| Param            | Type                              | Description                     |
| ---------------- | --------------------------------- | ------------------------------- |
| **eventName**    | "onInterstitialAdLeftApplication" | onInterstitialAdLeftApplication |
| **listenerFunc** | (info: any) => void               |                                 |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Prepared RewardedVideo

| Param            | Type                      | Description             |
| ---------------- | ------------------------- | ----------------------- |
| **eventName**    | "onRewardedVideoAdLoaded" | onRewardedVideoAdLoaded |
| **listenerFunc** | (info: any) => void       |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is opened

| Param            | Type                      | Description             |
| ---------------- | ------------------------- | ----------------------- |
| **eventName**    | "onRewardedVideoAdOpened" | onRewardedVideoAdOpened |
| **listenerFunc** | (info: any) => void       |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo go to background(Android)

| Param            | Type                  | Description             |
| ---------------- | --------------------- | ----------------------- |
| **eventName**    | "onAdLeftApplication" | onRewardedVideoAdOpened |
| **listenerFunc** | (info: any) => void   |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoStarted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is started

| Param            | Type                     | Description            |
| ---------------- | ------------------------ | ---------------------- |
| **eventName**    | "onRewardedVideoStarted" | onRewardedVideoStarted |
| **listenerFunc** | (info: any) => void      |                        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is closed

| Param            | Type                      | Description             |
| ---------------- | ------------------------- | ----------------------- |
| **eventName**    | "onRewardedVideoAdClosed" | onRewardedVideoAdClosed |
| **listenerFunc** | (info: any) => void       |                         |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewarded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: User get reward by RewardedVideo

| Param            | Type                | Description |
| ---------------- | ------------------- | ----------- |
| **eventName**    | "onRewarded"        | onRewarded  |
| **listenerFunc** | (info: any) => void |             |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: click link of RewardedVideo ad

| Param            | Type                               | Description                      |
| ---------------- | ---------------------------------- | -------------------------------- |
| **eventName**    | "onRewardedVideoAdLeftApplication" | onRewardedVideoAdLeftApplication |
| **listenerFunc** | (info: any) => void                |                                  |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Failed to load RewardVideo ad

| Param            | Type                            | Description                   |
| ---------------- | ------------------------------- | ----------------------------- |
| **eventName**    | "onRewardedVideoAdFailedToLoad" | onRewardedVideoAdFailedToLoad |
| **listenerFunc** | (info: any) => void             |                               |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoCompleted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Watch RewardVideo complete

| Param            | Type                       | Description              |
| ---------------- | -------------------------- | ------------------------ |
| **eventName**    | "onRewardedVideoCompleted" | onRewardedVideoCompleted |
| **listenerFunc** | (info: any) => void        |                          |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### Interfaces


#### AdMobInitializationOptions

| Prop                             | Type     | Description                                                                                                                                                                                                      |
| -------------------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **requestTrackingAuthorization** | boolean  | Use or not requestTrackingAuthorization in iOS(>14)                                                                                                                                                              |
| **testingDevices**               | string[] | An Array of devices IDs that will be marked as tested devices if {@link AdMobInitializationOptions.initializeForTesting} is true (Real Ads will be served to Testing devices but they will not count as 'real'). |
| **initializeForTesting**         | boolean  | If set to true, the devices on {@link AdMobInitializationOptions.testingDevices} will be registered to receive test production ads.                                                                              |


#### AdOptions

| Prop          | Type                      | Description                                                                                                                                   | Default |
| ------------- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| **adId**      | string                    | The ad unit ID that you want to request                                                                                                       |         |
| **adSize**    | [AdSize](#adsize)         | Banner Ad Size, defaults to SMART_BANNER. IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM  |         |
| **position**  | [AdPosition](#adposition) |                                                                                                                                               |         |
| **isTesting** | boolean                   |                                                                                                                                               |         |
| **margin**    | number                    | Margin Banner. Default is 0 px; If position is BOTTOM_CENTER, margin is be margin-bottom. If position is TOP_CENTER, margin is be margin-top. |         |
| **npa**       | boolean                   | The default behavior of the Google Mobile Ads SDK is to serve personalized ads. Set this to true to request Non-Personalized Ads              | false   |


#### PluginListenerHandle

| Prop       | Type       |
| ---------- | ---------- |
| **remove** | () => void |


### Enums


#### AdSize

| Members              | Value              |
| -------------------- | ------------------ |
| **BANNER**           | 'BANNER'           |
| **FLUID**            | 'FLUID'            |
| **FULL_BANNER**      | 'FULL_BANNER'      |
| **LARGE_BANNER**     | 'LARGE_BANNER'     |
| **LEADERBOARD**      | 'LEADERBOARD'      |
| **MEDIUM_RECTANGLE** | 'MEDIUM_RECTANGLE' |
| **SMART_BANNER**     | 'SMART_BANNER'     |
| **CUSTOM**           | 'CUSTOM'           |


#### AdPosition

| Members           | Value           |
| ----------------- | --------------- |
| **TOP_CENTER**    | 'TOP_CENTER'    |
| **CENTER**        | 'CENTER'        |
| **BOTTOM_CENTER** | 'BOTTOM_CENTER' |


<!--DOCGEN_API_END-->
