import type { PluginListenerHandle } from '@capacitor/core';

import type { NativeAdEvent, NativeAdErrorEvent, NativeAdRevenueEvent } from './native-ad-event.interface';
import type { NativeAdCommandIdentity, NativeAdFeedSession, NativeAdLoadOptions } from './native-ad-options.interface';
import type { NativeAdPlacementBatch } from './native-ad-placement.interface';
import type { NativeAdPluginEvents } from './native-ad-plugin-events.enum';

/** Low-level native bridge used by {@link NativeAdFeed}. */
export interface NativeAdDefinitions {
  startNativeAdFeed(options: NativeAdFeedSession): Promise<void>;
  destroyNativeAdFeed(options: NativeAdFeedSession): Promise<void>;
  loadNativeAd(options: NativeAdLoadOptions): Promise<void>;
  updateNativeAdPlacements(options: NativeAdPlacementBatch): Promise<void>;
  removeNativeAd(options: NativeAdCommandIdentity): Promise<void>;

  addListener(
    eventName: NativeAdPluginEvents,
    listenerFunc: (info: (NativeAdEvent | NativeAdErrorEvent | NativeAdRevenueEvent) & NativeAdFeedSession) => void,
  ): Promise<PluginListenerHandle>;
}
