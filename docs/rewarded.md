---
title: Rewarded Ads
---

# Rewarded Ads

Treat rewarded ads as a reward flow, not as another non-rewarded interstitial. Grant the reward only from the returned result or the `Rewarded` event, not from `Dismissed`.

## Rewarded video

Use a rewarded ad for a dedicated reward flow.

```ts
import {
  AdLoadInfo,
  AdMob,
  AdMobRevenueData,
  AdMobRewardItem,
  RewardAdOptions,
  RewardAdPluginEvents,
} from '@capacitor-community/admob';

await AdMob.addListener(RewardAdPluginEvents.Loaded, (info: AdLoadInfo) => {
  console.log('Rewarded ad loaded', info.adUnitId);
});
await AdMob.addListener(RewardAdPluginEvents.FailedToLoad, console.error);
await AdMob.addListener(RewardAdPluginEvents.Rewarded, (reward: AdMobRewardItem) => {
  console.log('Reward earned', reward.amount, reward.type);
});
await AdMob.addListener(RewardAdPluginEvents.AdImpression, (data: AdMobRevenueData) => {
  console.log(data);
});

const options: RewardAdOptions = {
  adId: 'YOUR_AD_UNIT_ID',
  // isTesting: true,
  // npa: true,
  // immersiveMode: true,
  // ssv: {
  //   userId: 'USER_ID',
  //   customData: JSON.stringify({ placement: 'bonus' }),
  // },
};
await AdMob.prepareRewardVideoAd(options);
const rewardItem = await AdMob.showRewardVideoAd();
// Grant the reward once, using this result or the Rewarded event — not both.
console.log(rewardItem);
```

When no `adId` is passed to `showRewardVideoAd()`, the most recently prepared ad is shown.

### Prepare more than one ad

```ts
await AdMob.prepareRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-1' });
await AdMob.prepareRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-2' });

const reward = await AdMob.showRewardVideoAd({ adId: 'ca-app-pub-xxx/reward-1' });
```

## Rewarded interstitial

Use a rewarded interstitial when the rewarded experience belongs at a natural transition in the app.

```ts
import {
  AdMob,
  AdMobRewardInterstitialItem,
  RewardInterstitialAdOptions,
  RewardInterstitialAdPluginEvents,
} from '@capacitor-community/admob';

await AdMob.addListener(RewardInterstitialAdPluginEvents.FailedToLoad, console.error);

const options: RewardInterstitialAdOptions = {
  adId: 'YOUR_AD_UNIT_ID',
};
const { adUnitId } = await AdMob.prepareRewardInterstitialAd(options);
const rewardItem: AdMobRewardInterstitialItem = await AdMob.showRewardInterstitialAd({
  adId: adUnitId,
});
console.log(rewardItem);
```

Request fields are defined on [`RewardAdOptions`](../README.md#rewardadoptions) and [`RewardInterstitialAdOptions`](../README.md#rewardinterstitialadoptions). See [Testing](./testing.md) for `isTesting`.

## Server-side verification

Server-side verification (SSV) callbacks fire only for production ads. Test ads do not invoke your SSV endpoint.

For local validation of the `ssv` payload, you can send a mock request after `RewardAdPluginEvents.Rewarded`. Replace `ENVIRONMENT_IS_DEVELOPMENT` with your own development flag:

```ts
const userId = 'USER_ID';
const customData = JSON.stringify({ placement: 'bonus' });

await AdMob.addListener(RewardAdPluginEvents.Rewarded, async () => {
  if (!ENVIRONMENT_IS_DEVELOPMENT) {
    return;
  }
  try {
    const params = new URLSearchParams({
      ad_network: 'TEST',
      ad_unit: 'TEST',
      custom_data: customData,
      reward_amount: 'TEST',
      reward_item: 'TEST',
      timestamp: 'TEST',
      transaction_id: 'TEST',
      user_id: userId,
      signature: 'TEST',
      key_id: 'TEST',
    });
    await fetch(`https://your-staging-ssv-endpoint?${params.toString()}`);
  } catch (err) {
    console.error(err);
  }
});
```
