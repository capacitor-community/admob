import { registerPlugin } from '@capacitor/core';

import type { AdMobPlugin } from './definitions';
import type { NativeAdDefinitions } from './native-ads/native-ad-definitions.interface';
import { configureNativeAdBridge } from './native-ads/native-ad-feed';

const nativeAdBridge = registerPlugin<AdMobPlugin & NativeAdDefinitions>('AdMob', {
  web: () => import('./web').then((m) => new m.AdMobWeb() as AdMobPlugin & NativeAdDefinitions),
});
const AdMob: AdMobPlugin = nativeAdBridge;

configureNativeAdBridge(nativeAdBridge);

export * from './definitions';
export * from './banner/index';
export * from './interstitial/index';
export * from './reward-interstitial/index';
export * from './reward/index';
export * from './consent/index';
export * from './shared/index';
export * from './app-open/index';
export * from './native-ads/index';
export { AdMob };
