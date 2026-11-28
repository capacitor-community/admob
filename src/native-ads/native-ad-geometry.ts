import { measureProjectionFrame } from '@webview-native-ui/engine';

import type { NativeAdPlacementBatch, NativeAdRect } from './native-ad-placement.interface';

export interface MeasuredNativeAdSlot {
  rect: NativeAdRect;
  clipRect: NativeAdRect;
}

export const measureNativeAdSlot = (
  element: HTMLElement,
  scrollElement?: HTMLElement,
  visibleArea?: NativeAdRect,
): MeasuredNativeAdSlot | undefined => {
  const { frame: rect, clip: clipRect, visible } = measureProjectionFrame(element, { scrollOwner: scrollElement, visibleArea });
  if (!visible) return undefined;
  if (scrollElement) {
    const origin = scrollElement.getBoundingClientRect();
    const translate = (value: NativeAdRect): NativeAdRect => ({
      ...value,
      x: value.x - origin.left + scrollElement.scrollLeft,
      y: value.y - origin.top + scrollElement.scrollTop,
    });
    return { rect: translate(rect), clipRect: translate(clipRect) };
  }
  return { rect, clipRect };
};

export const measureNativeAdScrollContainer = (
  element: HTMLElement,
  visibleArea?: NativeAdRect,
): NativeAdPlacementBatch['scrollContainer'] => {
  const { frame: rect, clip: clipRect } = measureProjectionFrame(element, { visibleArea });
  return { rect, clipRect, contentWidth: element.scrollWidth, contentHeight: element.scrollHeight };
};
