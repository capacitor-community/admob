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
