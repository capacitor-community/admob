import { registerPlugin } from '@capacitor/core';

import type { AdMobPlugin } from './definitions';

const AdMob = registerPlugin<AdMobPlugin>('AdMob', {
  web: () => import('./web').then(m => new m.AdMobWeb()),
});

export * from './definitions';
export { AdMob };
