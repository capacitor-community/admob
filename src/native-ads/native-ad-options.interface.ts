import type { AdOptions } from '../shared';

import type { NativeAdStyle } from './native-ad-style.interface';
import type { NativeAdTemplate } from './native-ad-template.enum';

/** @experimental Native feed ads are not yet covered by the stable API contract. */
export interface NativeAdFeedOptions extends Pick<AdOptions, 'npa'> {
  /** Unique ID for this feed manager. */
  feedId: string;

  /** Plugin-owned layout used for every native ad in this feed. */
  template?: NativeAdTemplate;

  /** Cross-platform styling applied to the plugin-owned layout. */
  style?: NativeAdStyle;

  /** Required while this research preview is restricted to Google's test ads. */
  isTesting: true;
}

export interface NativeAdFeedSession {
  feedId: string;
  sessionId: string;
}

export interface NativeAdLoadOptions extends Pick<AdOptions, 'npa'>, NativeAdFeedSession {
  slotKey: string;
  template: NativeAdTemplate;
  style?: NativeAdStyle;
  isTesting: true;
}

export interface NativeAdIdentity {
  feedId: string;
  slotKey: string;
}

export interface NativeAdCommandIdentity extends NativeAdIdentity, NativeAdFeedSession {}
