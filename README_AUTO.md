This API is created by https://www.npmjs.com/package/@capacitor/docgen
This README is developing test.

## Install

```bash
npm install test
npx cap sync
```

## API

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
* [`pauseRewardedVideo()`](#pauserewardedvideo)
* [`resumeRewardedVideo()`](#resumerewardedvideo)
* [`stopRewardedVideo()`](#stoprewardedvideo)
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

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: AdMobInitializationOptions) => Promise<{ value: boolean; }>
```

Initialize AdMob with <a href="#admobinitializationoptions">AdMobInitializationOptions</a>

| Param         | Type                                                                              | Description                                                          |
| ------------- | --------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| **`options`** | <code><a href="#admobinitializationoptions">AdMobInitializationOptions</a></code> | <a href="#admobinitializationoptions">AdMobInitializationOptions</a> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### showBanner(...)

```typescript
showBanner(options: AdOptions) => Promise<{ value: boolean; }>
```

Show a banner Ad

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### hideBanner()

```typescript
hideBanner() => Promise<{ value: boolean; }>
```

Hide the banner, remove it from screen, but can show it later

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### resumeBanner()

```typescript
resumeBanner() => Promise<{ value: boolean; }>
```

Resume the banner, show it after hide

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### removeBanner()

```typescript
removeBanner() => Promise<{ value: boolean; }>
```

Destroy the banner, remove it from screen.

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### prepareInterstitial(...)

```typescript
prepareInterstitial(options: AdOptions) => Promise<{ value: boolean; }>
```

Prepare interstitial banner

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### showInterstitial()

```typescript
showInterstitial() => Promise<{ value: boolean; }>
```

Show interstitial ad when it’s ready

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### prepareRewardVideoAd(...)

```typescript
prepareRewardVideoAd(options: AdOptions) => Promise<{ value: boolean; }>
```

Prepare a reward video ad

| Param         | Type                                            | Description                        |
| ------------- | ----------------------------------------------- | ---------------------------------- |
| **`options`** | <code><a href="#adoptions">AdOptions</a></code> | <a href="#adoptions">AdOptions</a> |

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### showRewardVideoAd()

```typescript
showRewardVideoAd() => Promise<{ value: boolean; }>
```

Show a reward video ad

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### pauseRewardedVideo()

```typescript
pauseRewardedVideo() => Promise<{ value: boolean; }>
```

Pause RewardedVideo

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### resumeRewardedVideo()

```typescript
resumeRewardedVideo() => Promise<{ value: boolean; }>
```

Resume RewardedVideo

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### stopRewardedVideo()

```typescript
stopRewardedVideo() => Promise<{ value: boolean; }>
```

Close RewardedVideo

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: banner ad is loaded(android)

| Param              | Type                                | Description |
| ------------------ | ----------------------------------- | ----------- |
| **`eventName`**    | <code>"onAdLoaded"</code>           | onAdLoaded  |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onAdFailedToLoad', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: failed to load Banner ad(android)

| Param              | Type                                | Description      |
| ------------------ | ----------------------------------- | ---------------- |
| **`eventName`**    | <code>"onAdFailedToLoad"</code>     | onAdFailedToLoad |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |                  |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: banner ad is show(android)

| Param              | Type                                | Description |
| ------------------ | ----------------------------------- | ----------- |
| **`eventName`**    | <code>"onAdOpened"</code>           | onAdOpened  |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Banner ad is closed(android)

| Param              | Type                                | Description |
| ------------------ | ----------------------------------- | ----------- |
| **`eventName`**    | <code>"onAdClosed"</code>           | onAdClosed  |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adViewDidReceiveAd', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: request loaded Banner ad(iOS)

| Param              | Type                                | Description        |
| ------------------ | ----------------------------------- | ------------------ |
| **`eventName`**    | <code>"adViewDidReceiveAd"</code>   | adViewDidReceiveAd |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |                    |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adView:didFailToReceiveAdWithError', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: request failed Banner ad(iOS)

| Param              | Type                                              | Description                        |
| ------------------ | ------------------------------------------------- | ---------------------------------- |
| **`eventName`**    | <code>"adView:didFailToReceiveAdWithError"</code> | adView:didFailToReceiveAdWithError |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>               |                                    |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adViewWillPresentScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: full-screen view will be presented in response to the user clicking on an ad.(iOS)

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"adViewWillPresentScreen"</code> | adViewWillPresentScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>    |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adViewWillDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen view will be dismissed.(iOS)

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"adViewWillDismissScreen"</code> | adViewWillDismissScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>    |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'adViewDidDismissScreen', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: The full-screen view has been dismissed.(iOS)

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"adViewDidDismissScreen"</code> | adViewDidDismissScreen |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>   |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewarded', listenerFunc: (adMobRewardItem: AdMobRewardItem) => void) => PluginListenerHandle
```

Notice: User click will open another app.(iOS)

| Param              | Type                                                                                      | Description                |
| ------------------ | ----------------------------------------------------------------------------------------- | -------------------------- |
| **`eventName`**    | <code>"onRewarded"</code>                                                                 | adViewWillLeaveApplication |
| **`listenerFunc`** | <code>(adMobRewardItem: <a href="#admobrewarditem">AdMobRewardItem</a>) =&gt; void</code> |                            |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad loaded

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"onInterstitialAdLoaded"</code> | onInterstitialAdLoaded |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>   |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.2.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad opened

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"onInterstitialAdOpened"</code> | onInterstitialAdOpened |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>   |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.2.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Interstitial ad closed

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"onInterstitialAdClosed"</code> | onInterstitialAdClosed |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>   |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.2.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Click link of Interstitial ad

| Param              | Type                                           | Description                     |
| ------------------ | ---------------------------------------------- | ------------------------------- |
| **`eventName`**    | <code>"onInterstitialAdLeftApplication"</code> | onInterstitialAdLeftApplication |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>            |                                 |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.2.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onInterstitialAdFailedToLoad', listenerFunc: (info: { error: string; errorCode: number; }) => void) => PluginListenerHandle
```

Notice: Failed to load Interstitial ad

| Param              | Type                                                                  | Description                  |
| ------------------ | --------------------------------------------------------------------- | ---------------------------- |
| **`eventName`**    | <code>"onInterstitialAdFailedToLoad"</code>                           | onInterstitialAdFailedToLoad |
| **`listenerFunc`** | <code>(info: { error: string; errorCode: number; }) =&gt; void</code> |                              |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdLoaded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Prepared RewardedVideo

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdLoaded"</code> | onRewardedVideoAdLoaded |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>    |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdOpened', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is opened

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdOpened"</code> | onRewardedVideoAdOpened |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>    |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo go to background(Android)

| Param              | Type                                | Description             |
| ------------------ | ----------------------------------- | ----------------------- |
| **`eventName`**    | <code>"onAdLeftApplication"</code>  | onRewardedVideoAdOpened |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoStarted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is started

| Param              | Type                                  | Description            |
| ------------------ | ------------------------------------- | ---------------------- |
| **`eventName`**    | <code>"onRewardedVideoStarted"</code> | onRewardedVideoStarted |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>   |                        |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdClosed', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: RewardedVideo is closed

| Param              | Type                                   | Description             |
| ------------------ | -------------------------------------- | ----------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdClosed"</code> | onRewardedVideoAdClosed |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>    |                         |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewarded', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: User get reward by RewardedVideo

| Param              | Type                                | Description |
| ------------------ | ----------------------------------- | ----------- |
| **`eventName`**    | <code>"onRewarded"</code>           | onRewarded  |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code> |             |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdLeftApplication', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: click link of RewardedVideo ad

| Param              | Type                                            | Description                      |
| ------------------ | ----------------------------------------------- | -------------------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdLeftApplication"</code> | onRewardedVideoAdLeftApplication |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>             |                                  |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoAdFailedToLoad', listenerFunc: (error: AdMobError) => void) => PluginListenerHandle
```

Notice: Failed to load RewardVideo ad

| Param              | Type                                                                  | Description                   |
| ------------------ | --------------------------------------------------------------------- | ----------------------------- |
| **`eventName`**    | <code>"onRewardedVideoAdFailedToLoad"</code>                          | onRewardedVideoAdFailedToLoad |
| **`listenerFunc`** | <code>(error: <a href="#admoberror">AdMobError</a>) =&gt; void</code> |                               |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onRewardedVideoCompleted', listenerFunc: (info: any) => void) => PluginListenerHandle
```

Notice: Watch RewardVideo complete

| Param              | Type                                    | Description              |
| ------------------ | --------------------------------------- | ------------------------ |
| **`eventName`**    | <code>"onRewardedVideoCompleted"</code> | onRewardedVideoCompleted |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>     |                          |

**Returns:** <code><a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

**Since:** 1.1.2

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


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### AdMobRewardItem

| Prop         | Type                |
| ------------ | ------------------- |
| **`type`**   | <code>string</code> |
| **`amount`** | <code>number</code> |


#### AdMobError

| Prop         | Type                |
| ------------ | ------------------- |
| **`reason`** | <code>string</code> |
| **`code`**   | <code>number</code> |


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
| **`SMART_BANNER`**     | <code>'SMART_BANNER'</code>     | A dynamically sized banner that is full-width and auto-height.                                                                                 |
| **`CUSTOM`**           | <code>'CUSTOM'</code>           | To define a custom banner size, set your desired <a href="#adsize">AdSize</a>                                                                  |


#### AdPosition

| Members             | Value                        | Description                               |
| ------------------- | ---------------------------- | ----------------------------------------- |
| **`TOP_CENTER`**    | <code>'TOP_CENTER'</code>    | Banner position be top-center             |
| **`CENTER`**        | <code>'CENTER'</code>        | Banner position be center                 |
| **`BOTTOM_CENTER`** | <code>'BOTTOM_CENTER'</code> | Banner position be bottom-center(default) |

</docgen-api>
