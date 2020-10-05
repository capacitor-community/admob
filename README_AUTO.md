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

| Param       | Type                                                      |
| ----------- | --------------------------------------------------------- |
| **options** | [AdMobInitializationOptions](#admobinitializationoptions) |

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### showBanner

```typescript
showBanner(options: AdOptions) => Promise<{ value: boolean; }>
```

| Param       | Type                    |
| ----------- | ----------------------- |
| **options** | [AdOptions](#adoptions) |

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### hideBanner

```typescript
hideBanner() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### resumeBanner

```typescript
resumeBanner() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### removeBanner

```typescript
removeBanner() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### prepareInterstitial

```typescript
prepareInterstitial(options: AdOptions) => Promise<{ value: boolean; }>
```

| Param       | Type                    |
| ----------- | ----------------------- |
| **options** | [AdOptions](#adoptions) |

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### showInterstitial

```typescript
showInterstitial() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### prepareRewardVideoAd

```typescript
prepareRewardVideoAd(options: AdOptions) => Promise<{ value: boolean; }>
```

| Param       | Type                    |
| ----------- | ----------------------- |
| **options** | [AdOptions](#adoptions) |

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### showRewardVideoAd

```typescript
showRewardVideoAd() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### pauseRewardedVideo

```typescript
pauseRewardedVideo() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### resumeRewardedVideo

```typescript
resumeRewardedVideo() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### stopRewardedVideo

```typescript
stopRewardedVideo() => Promise<{ value: boolean; }>
```

**Returns:** Promise&lt;{ value: boolean; }&gt;

--------------------


### addListener

```typescript
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                |
| ---------------- | ------------------- |
| **eventName**    | "onAdLoaded"        |
| **listenerFunc** | (info: any) => void |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                |
| ---------------- | ------------------- |
| **eventName**    | "onAdFailedToLoad"  |
| **listenerFunc** | (info: any) => void |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                |
| ---------------- | ------------------- |
| **eventName**    | "onAdOpened"        |
| **listenerFunc** | (info: any) => void |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                |
| ---------------- | ------------------- |
| **eventName**    | "onAdClosed"        |
| **listenerFunc** | (info: any) => void |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                      |
| ---------------- | ------------------------- |
| **eventName**    | "onRewardedVideoAdLoaded" |
| **listenerFunc** | (info: any) => void       |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                      |
| ---------------- | ------------------------- |
| **eventName**    | "onRewardedVideoAdOpened" |
| **listenerFunc** | (info: any) => void       |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                  |
| ---------------- | --------------------- |
| **eventName**    | "onAdLeftApplication" |
| **listenerFunc** | (info: any) => void   |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoStarted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                     |
| ---------------- | ------------------------ |
| **eventName**    | "onRewardedVideoStarted" |
| **listenerFunc** | (info: any) => void      |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                      |
| ---------------- | ------------------------- |
| **eventName**    | "onRewardedVideoAdClosed" |
| **listenerFunc** | (info: any) => void       |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewarded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                |
| ---------------- | ------------------- |
| **eventName**    | "onRewarded"        |
| **listenerFunc** | (info: any) => void |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                               |
| ---------------- | ---------------------------------- |
| **eventName**    | "onRewardedVideoAdLeftApplication" |
| **listenerFunc** | (info: any) => void                |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                            |
| ---------------- | ------------------------------- |
| **eventName**    | "onRewardedVideoAdFailedToLoad" |
| **listenerFunc** | (info: any) => void             |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'onRewardedVideoCompleted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                       |
| ---------------- | -------------------------- |
| **eventName**    | "onRewardedVideoCompleted" |
| **listenerFunc** | (info: any) => void        |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewDidReceiveAd', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                 |
| ---------------- | -------------------- |
| **eventName**    | "adViewDidReceiveAd" |
| **listenerFunc** | (info: any) => void  |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adView:didFailToReceiveAdWithError', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                                 |
| ---------------- | ------------------------------------ |
| **eventName**    | "adView:didFailToReceiveAdWithError" |
| **listenerFunc** | (info: any) => void                  |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillPresentScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                      |
| ---------------- | ------------------------- |
| **eventName**    | "adViewWillPresentScreen" |
| **listenerFunc** | (info: any) => void       |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                      |
| ---------------- | ------------------------- |
| **eventName**    | "adViewWillDismissScreen" |
| **listenerFunc** | (info: any) => void       |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewDidDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                     |
| ---------------- | ------------------------ |
| **eventName**    | "adViewDidDismissScreen" |
| **listenerFunc** | (info: any) => void      |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### addListener

```typescript
addListener(eventName: 'adViewWillLeaveApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

| Param            | Type                         |
| ---------------- | ---------------------------- |
| **eventName**    | "adViewWillLeaveApplication" |
| **listenerFunc** | (info: any) => void          |

**Returns:** [PluginListenerHandle](#pluginlistenerhandle)

--------------------


### Interfaces


#### AdMobInitializationOptions

| Prop                             | Type     | Description                                                                                                                                                                                                      |
| -------------------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **requestTrackingAuthorization** | boolean  |                                                                                                                                                                                                                  |
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
