import type { AdOptions } from '../shared';

import type { NativeAdRect } from './native-ad-placement.interface';
import type { NativeAdStyle } from './native-ad-style.interface';
import type { NativeAdTemplate } from './native-ad-template.enum';

/** Options for a native ad feed. */
export interface NativeAdFeedOptions extends Pick<AdOptions, 'npa' | 'isTesting'> {
  /** Unique ID for this feed manager. */
  feedId: string;

  /**
   * iOS native tracking for a single scroll container.
   * For Ionic, pass the result of ion-content.getScrollElement().
   * Other platforms retain JavaScript placement updates.
   */
  scrollElement?: HTMLElement;

  /** Android: use a cached DOM image while scrolling. */
  androidScrollSnapshot?: boolean;

  /**
   * The part of the viewport, in CSS pixels, that native ads may be drawn in.
   * Use it to keep ads clear of app chrome that the page scrolls under, such
   * as a translucent header or a floating tab bar. Called on every placement
   * update; return `undefined` to use the whole viewport. An ad with no part
   * inside the area is hidden.
   */
  visibleArea?: () => NativeAdRect | undefined;

  /** Plugin-owned layout used for every native ad in this feed. */
  template?: NativeAdTemplate;

  /** Cross-platform styling applied to the plugin-owned layout. */
  style?: NativeAdStyle;

  /** Native ad unit ID. Required unless isTesting is true. */
  adId?: string;
}

export interface NativeAdFeedSession {
  feedId: string;
  sessionId: string;
}

export interface NativeAdLoadOptions extends Pick<AdOptions, 'npa' | 'isTesting'>, NativeAdFeedSession {
  slotKey: string;
  /** Internal Feed projection destination; applications use attach(). */
  projectionSelector?: string;
  template: NativeAdTemplate;
  style?: NativeAdStyle;
  adId?: string;
}

export interface NativeAdIdentity {
  feedId: string;
  slotKey: string;
}

export interface NativeAdCommandIdentity extends NativeAdIdentity, NativeAdFeedSession {}
