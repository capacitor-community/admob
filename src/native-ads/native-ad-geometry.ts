import type { NativeAdPlacementBatch, NativeAdRect } from './native-ad-placement.interface';

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

export function* nativeAdAncestors(element: HTMLElement): Generator<HTMLElement> {
  let current: HTMLElement | null = element;
  while (current) {
    const root: Node = current.getRootNode();
    current =
      current.assignedSlot ?? current.parentElement ?? (root instanceof ShadowRoot ? (root.host as HTMLElement) : null);
    if (current) yield current;
  }
}

export const measureNativeAdSlot = (
  element: HTMLElement,
  scrollElement?: HTMLElement,
): MeasuredNativeAdSlot | undefined => {
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
  let clipRect: NativeAdRect = scrollElement
    ? rect
    : {
        x: window.visualViewport?.offsetLeft ?? 0,
        y: window.visualViewport?.offsetTop ?? 0,
        width: window.visualViewport?.width ?? window.innerWidth,
        height: window.visualViewport?.height ?? window.innerHeight,
      };

  let foundScrollElement = !scrollElement;
  for (const ancestor of nativeAdAncestors(element)) {
    if (ancestor === scrollElement) {
      foundScrollElement = true;
      break;
    }
    if (clipsDescendants(ancestor)) {
      clipRect = intersectRects(clipRect, toNativeRect(ancestor.getBoundingClientRect()));
    }
  }

  clipRect = intersectRects(rect, clipRect);
  if (!foundScrollElement || clipRect.width <= 0 || clipRect.height <= 0) return undefined;
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

export const measureNativeAdScrollContainer = (element: HTMLElement): NativeAdPlacementBatch['scrollContainer'] => {
  const rect = toNativeRect(element.getBoundingClientRect());
  let clipRect = intersectRects(rect, {
    x: window.visualViewport?.offsetLeft ?? 0,
    y: window.visualViewport?.offsetTop ?? 0,
    width: window.visualViewport?.width ?? window.innerWidth,
    height: window.visualViewport?.height ?? window.innerHeight,
  });
  for (const ancestor of nativeAdAncestors(element)) {
    if (clipsDescendants(ancestor)) {
      clipRect = intersectRects(clipRect, toNativeRect(ancestor.getBoundingClientRect()));
    }
  }
  return { rect, clipRect, contentWidth: element.scrollWidth, contentHeight: element.scrollHeight };
};
