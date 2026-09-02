import type { NativeAdRect } from './native-ad-placement.interface';

export interface MeasuredNativeAdSlot {
  rect: NativeAdRect;
  clipRect: NativeAdRect;
}

const intersectRects = (left: NativeAdRect, right: NativeAdRect): NativeAdRect => {
  const x = Math.max(left.x, right.x);
  const y = Math.max(left.y, right.y);
  const maxX = Math.min(left.x + left.width, right.x + right.width);
  const maxY = Math.min(left.y + left.height, right.y + right.height);
  return {
    x,
    y,
    width: Math.max(0, maxX - x),
    height: Math.max(0, maxY - y),
  };
};

const toNativeRect = (rect: DOMRect): NativeAdRect => ({
  x: rect.left,
  y: rect.top,
  width: rect.width,
  height: rect.height,
});

const clipsDescendants = (element: HTMLElement): boolean => {
  const style = window.getComputedStyle(element);
  return [style.overflow, style.overflowX, style.overflowY].some((value) =>
    ['auto', 'hidden', 'scroll', 'clip'].includes(value),
  );
};

export const measureNativeAdSlot = (element: HTMLElement): MeasuredNativeAdSlot | undefined => {
  const style = window.getComputedStyle(element);
  const domRect = element.getBoundingClientRect();
  if (
    !element.isConnected ||
    style.display === 'none' ||
    style.visibility === 'hidden' ||
    Number.parseFloat(style.opacity || '1') === 0 ||
    domRect.width <= 0 ||
    domRect.height <= 0
  ) {
    return undefined;
  }

  const rect = toNativeRect(domRect);
  let clipRect: NativeAdRect = {
    x: window.visualViewport?.offsetLeft ?? 0,
    y: window.visualViewport?.offsetTop ?? 0,
    width: window.visualViewport?.width ?? window.innerWidth,
    height: window.visualViewport?.height ?? window.innerHeight,
  };

  let ancestor = element.parentElement;
  while (ancestor) {
    if (clipsDescendants(ancestor)) {
      clipRect = intersectRects(clipRect, toNativeRect(ancestor.getBoundingClientRect()));
    }
    ancestor = ancestor.parentElement;
  }

  clipRect = intersectRects(rect, clipRect);
  return clipRect.width > 0 && clipRect.height > 0 ? { rect, clipRect } : undefined;
};
