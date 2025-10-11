import { registerPlugin } from '@capacitor/core';

import type { AdMobPlugin } from './definitions';

const AdMob = registerPlugin<AdMobPlugin>('AdMob', {
  web: () => import('./web').then((m) => new m.AdMobWeb()),
});

export * from './definitions';
export * from './banner/index';
export * from './interstitial/index';
export * from './reward-interstitial/index';
export * from './reward/index';
export * from './consent/index';
export * from './shared/index';
export * from './app-open/index';
export { AdMob };
