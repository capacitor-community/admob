import type { AdMobError, AdMobRevenueData } from '../shared';

import type { NativeAdIdentity } from './native-ad-options.interface';

export type NativeAdErrorEvent = NativeAdIdentity & AdMobError;
export type NativeAdRevenueEvent = NativeAdIdentity & AdMobRevenueData;
export type NativeAdEvent = NativeAdIdentity;
