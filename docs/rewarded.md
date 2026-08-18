---
title: Rewarded Ads
---

# Rewarded Ads

Treat rewarded ads as a reward flow, not as another non-rewarded interstitial placement. Grant the reward only from the rewarded result or event.

## Rewarded video

Use a Rewarded ad for a dedicated reward flow.

```ts
import { AdMob, RewardAdOptions, AdLoadInfo, RewardAdPluginEvents, AdMobRevenueData } from '@capacitor-community/admob';

export async function rewardVideo(): Promise<void> {
  AdMob.addListener(RewardAdPluginEvents.Loaded, (info: AdLoadInfo) => {
    // Subscribe prepared rewardVideo
  });

  AdMob.addListener(RewardAdPluginEvents.AdImpression, (data: AdMobRevenueData) => {
    // Forward impression-level revenue to your analytics provider.
    console.log(data);
  });

  const options: RewardAdOptions = {
    adId: 'YOUR ADID',
    // isTesting: true
    // npa: true
    // immersiveMode: true
    // ssv: {
    //   userId: "A user ID to send to your SSV"
    //   customData: JSON.stringify({ ...MyCustomData })
    //}
  };
  await AdMob.prepareRewardVideoAd(options);
  const rewardItem = await AdMob.showRewardVideoAd();
  // Grant the reward once, using this result.
  console.log(rewardItem);

  // You can also prepare multiple reward ads and show a specific one by passing its adId:
  await AdMob.prepareRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-1' });
  await AdMob.prepareRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-2' });

  // Show a specific prepared ad
  const reward = await AdMob.showRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-1' });

  // Or omit adId to show the most recently prepared one (default behavior)
  const reward2 = await AdMob.showRewardVideoAd();
}
```

## Rewarded interstitial

Use a Rewarded Interstitial ad when the rewarded experience belongs at a natural transition in the app.

```ts
import {
  AdMob,
  AdMobRewardInterstitialItem,
  AdMobRevenueData,
  RewardInterstitialAdOptions,
  RewardInterstitialAdPluginEvents,
} from '@capacitor-community/admob';

export async function rewardInterstitial(): Promise<void> {
  AdMob.addListener(RewardInterstitialAdPluginEvents.AdImpression, (data: AdMobRevenueData) => {
    // Forward impression-level revenue to your analytics provider.
    console.log(data);
  });

  const options: RewardInterstitialAdOptions = {
    adId: 'YOUR ADID',
  };
  const { adUnitId } = await AdMob.prepareRewardInterstitialAd(options);
  const rewardItem: AdMobRewardInterstitialItem = await AdMob.showRewardInterstitialAd({ adId: adUnitId });
  // Grant the reward once, using this result.
  console.log(rewardItem);
}
```

## Server-side verification

SSV callbacks are only fired on Production Adverts, therefore test Ads will not fire off your SSV callback.

For E2E tests or just for validating the data in your `RewardAdOptions` work as expected, you can add a custom GET
request to your mock endpoint after the `RewardAdPluginEvents.Rewarded` similar to this:

```ts
AdMob.addListener(RewardAdPluginEvents.Rewarded, async () => {
  // ...
  if (ENVIRONMENT_IS_DEVELOPMENT) {
    try {
      const url =
        `https://your-staging-ssv-endpoint` +
        new URLSearchParams({
          ad_network: 'TEST',
          ad_unit: 'TEST',
          custom_data: customData, // <-- passed CustomData
          reward_amount: 'TEST',
          reward_item: 'TEST',
          timestamp: 'TEST',
          transaction_id: 'TEST',
          user_id: userId, // <-- Passed UserID
          signature: 'TEST',
          key_id: 'TEST',
        });
      await fetch(url);
    } catch (err) {
      console.error(err);
    }
  }
  // ...
});
```
