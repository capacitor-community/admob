import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const bridge = {
  startNativeAdFeed: vi.fn().mockResolvedValue(undefined),
  destroyNativeAdFeed: vi.fn().mockResolvedValue(undefined),
  loadNativeAd: vi.fn().mockResolvedValue(undefined),
  updateNativeAdPlacements: vi.fn().mockResolvedValue(undefined),
  removeNativeAd: vi.fn().mockResolvedValue(undefined),
  addListener: vi.fn().mockResolvedValue({ remove: vi.fn().mockResolvedValue(undefined) }),
};

import { configureNativeAdBridge, NativeAdFeed } from '../../../../src/native-ads/native-ad-feed';
import { NativeAdTemplate } from '../../../../src/native-ads/native-ad-template.enum';
import { AdMobWeb } from '../../../../src/web';

class VisibleIntersectionObserver implements IntersectionObserver {
  readonly root = null;
  readonly rootMargin = '0px';
  readonly scrollMargin = '0px';
  readonly thresholds = [0];

  constructor(private readonly callback: IntersectionObserverCallback) {}

  observe(target: Element): void {
    this.callback([{ isIntersecting: true, target } as IntersectionObserverEntry], this);
  }

  setIntersecting(target: Element, isIntersecting: boolean): void {
    this.callback([{ isIntersecting, target } as IntersectionObserverEntry], this);
  }

  disconnect(): void {}
  takeRecords(): IntersectionObserverEntry[] {
    return [];
  }
  unobserve(): void {}
}

const settle = async (): Promise<void> => {
  await vi.advanceTimersByTimeAsync(150);
};

describe('NativeAdFeed', () => {
  const createdFeeds: NativeAdFeed[] = [];
  const observers: VisibleIntersectionObserver[] = [];

  beforeEach(() => {
    vi.useFakeTimers();
    vi.stubGlobal(
      'IntersectionObserver',
      class extends VisibleIntersectionObserver {
        constructor(callback: IntersectionObserverCallback) {
          super(callback);
          observers.push(this);
        }
      },
    );
    vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockReturnValue({
      x: 10,
      y: 20,
      top: 20,
      left: 10,
      right: 310,
      bottom: 340,
      width: 300,
      height: 320,
      toJSON: () => ({}),
    });
    bridge.loadNativeAd.mockClear();
    bridge.updateNativeAdPlacements.mockClear();
    bridge.removeNativeAd.mockClear();
    bridge.startNativeAdFeed.mockClear();
    bridge.destroyNativeAdFeed.mockClear();
    configureNativeAdBridge(bridge);
  });

  afterEach(async () => {
    await Promise.all(createdFeeds.splice(0).map((feed) => feed.destroy()));
    document.body.innerHTML = '';
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
    vi.useRealTimers();
    observers.length = 0;
  });

  it('loads only after the stable slot is visible and scroll has settled', async () => {
    const feed = await NativeAdFeed.create({
      feedId: 'feed-visible',
      template: NativeAdTemplate.Medium,
      isTesting: true,
    });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'article-42');
    document.body.appendChild(element);

    expect(bridge.loadNativeAd).not.toHaveBeenCalled();
    await settle();

    expect(bridge.loadNativeAd).toHaveBeenCalledOnce();
    expect(bridge.loadNativeAd).toHaveBeenCalledWith(
      expect.objectContaining({ slotKey: 'article-42', isTesting: true }),
    );
    expect(bridge.loadNativeAd.mock.calls[0][0]).not.toHaveProperty('adId');
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [expect.objectContaining({ slotKey: 'article-42', visible: true })],
      }),
    );
  });

  it('follows scrolling once per frame, clips the ad, and skips unchanged placements', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-scroll', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('div');
    document.body.appendChild(element);
    feed.attach('feed-ad-1', element);
    await settle();
    bridge.updateNativeAdPlacements.mockClear();

    let top = -20;
    element.getBoundingClientRect = () => new DOMRect(10, top, 300, 320);
    document.dispatchEvent(new Event('scroll'));
    top = -40;
    document.dispatchEvent(new Event('scroll'));
    expect(bridge.updateNativeAdPlacements).not.toHaveBeenCalled();
    await vi.advanceTimersByTimeAsync(20);
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledOnce();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [
          expect.objectContaining({
            visible: true,
            rect: { x: 10, y: -40, width: 300, height: 320 },
            clipRect: { x: 10, y: 0, width: 300, height: 280 },
          }),
        ],
      }),
    );
    document.dispatchEvent(new Event('scroll'));
    await settle();
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledOnce();
    expect(bridge.loadNativeAd).toHaveBeenCalledOnce();

    top = -400;
    document.dispatchEvent(new Event('scroll'));
    await vi.advanceTimersByTimeAsync(20);
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({ placements: [expect.objectContaining({ visible: false })] }),
    );
  });

  it('coalesces motion while a bridge update is pending and cancels frames on destroy', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-pending-scroll', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('div');
    document.body.appendChild(element);
    feed.attach('pending-ad', element);
    await settle();
    bridge.updateNativeAdPlacements.mockClear();

    let finishUpdate!: () => void;
    bridge.updateNativeAdPlacements.mockImplementationOnce(
      () =>
        new Promise<void>((resolve) => {
          finishUpdate = resolve;
        }),
    );
    let top = 30;
    element.getBoundingClientRect = () => new DOMRect(10, top, 300, 320);
    document.dispatchEvent(new Event('scroll'));
    await vi.advanceTimersByTimeAsync(20);
    top = 40;
    document.dispatchEvent(new Event('scroll'));
    await vi.advanceTimersByTimeAsync(20);
    top = 50;
    document.dispatchEvent(new Event('scroll'));
    await vi.advanceTimersByTimeAsync(20);
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledOnce();
    finishUpdate();
    await vi.advanceTimersByTimeAsync(0);
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledTimes(2);
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [expect.objectContaining({ rect: { x: 10, y: 50, width: 300, height: 320 } })],
      }),
    );

    document.dispatchEvent(new Event('scroll'));
    await feed.destroy();
    bridge.updateNativeAdPlacements.mockClear();
    await settle();
    expect(bridge.updateNativeAdPlacements).not.toHaveBeenCalled();
  });

  it('detaches the old logical slot before a recycled element uses a new slotKey', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-recycle', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'first-item');
    document.body.appendChild(element);
    await settle();

    element.setAttribute('slot-key', 'second-item');
    await vi.advanceTimersByTimeAsync(0);
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [expect.objectContaining({ slotKey: 'first-item', visible: false })],
      }),
    );

    await settle();
    expect(bridge.loadNativeAd).toHaveBeenCalledWith(expect.objectContaining({ slotKey: 'second-item' }));
  });

  it('tracks and clips a slotted ad inside a shadow scroll container, then removes its listener', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-shadow-scroll', isTesting: true });
    createdFeeds.push(feed);
    const host = document.createElement('div');
    const root = host.attachShadow({ mode: 'open' });
    const scroller = document.createElement('div');
    scroller.style.overflow = 'auto';
    scroller.appendChild(document.createElement('slot'));
    root.appendChild(scroller);
    scroller.getBoundingClientRect = () => ({
      x: 0,
      y: 100,
      top: 100,
      left: 0,
      right: 400,
      bottom: 300,
      width: 400,
      height: 200,
      toJSON: () => ({}),
    });
    const element = document.createElement('div');
    host.appendChild(element);
    document.body.appendChild(host);
    feed.attach('shadow-ad', element);
    await settle();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [
          expect.objectContaining({
            visible: true,
            clipRect: { x: 10, y: 100, width: 300, height: 200 },
          }),
        ],
      }),
    );

    element.getBoundingClientRect = () => new DOMRect(10, 150, 300, 320);
    scroller.dispatchEvent(new Event('scroll'));
    await vi.advanceTimersByTimeAsync(20);
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [
          expect.objectContaining({
            visible: true,
            clipRect: { x: 10, y: 150, width: 300, height: 150 },
          }),
        ],
      }),
    );
    await settle();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({ placements: [expect.objectContaining({ visible: true })] }),
    );

    feed.detach(element);
    await settle();
    bridge.updateNativeAdPlacements.mockClear();
    scroller.dispatchEvent(new Event('scroll'));
    await settle();
    expect(bridge.updateNativeAdPlacements).not.toHaveBeenCalled();
  });

  it('does not retry a failed load until the app explicitly reloads the slot', async () => {
    bridge.loadNativeAd.mockRejectedValueOnce(new Error('network'));
    const feed = await NativeAdFeed.create({ feedId: 'feed-retry', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'failed-item');
    document.body.appendChild(element);
    await settle();

    document.dispatchEvent(new Event('scroll'));
    await settle();
    expect(bridge.loadNativeAd).toHaveBeenCalledOnce();

    await feed.reload('failed-item');
    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(2);
  });

  it('can retry removal after the native bridge rejects once', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-remove-error', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'remove-error-item');
    document.body.appendChild(element);
    await settle();

    bridge.removeNativeAd.mockRejectedValueOnce(new Error('bridge unavailable'));
    await expect(feed.reload('remove-error-item')).rejects.toThrow('bridge unavailable');
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(expect.objectContaining({ placements: [] }));
    await feed.reload('remove-error-item');

    expect(bridge.removeNativeAd).toHaveBeenCalledTimes(2);
    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(2);
  });

  it('recovers from a rejected placement update without an unhandled promise', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-placement-error', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'placement-error-item');
    document.body.appendChild(element);
    await settle();

    element.getBoundingClientRect = () => new DOMRect(10, 30, 300, 320);
    bridge.updateNativeAdPlacements.mockRejectedValueOnce(new Error('bridge unavailable'));
    document.dispatchEvent(new Event('scroll'));
    await settle();

    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({
        placements: [expect.objectContaining({ visible: true })],
      }),
    );
  });

  it('bounds detached slot state and loads an evicted logical slot again', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'feed-cache', isTesting: true });
    createdFeeds.push(feed);

    for (let index = 0; index < 4; index += 1) {
      const element = document.createElement('capacitor-admob-native');
      element.setAttribute('feed-id', feed.feedId);
      element.setAttribute('slot-key', `cached-item-${index}`);
      document.body.appendChild(element);
      await settle();
      element.remove();
      await settle();
    }

    const evictedElement = document.createElement('capacitor-admob-native');
    evictedElement.setAttribute('feed-id', feed.feedId);
    evictedElement.setAttribute('slot-key', 'cached-item-0');
    document.body.appendChild(evictedElement);
    await settle();

    expect(bridge.removeNativeAd).toHaveBeenCalledWith(
      expect.objectContaining({ feedId: feed.feedId, slotKey: 'cached-item-0' }),
    );
    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(5);
  });

  it('scopes every placement batch to the feed that produced it', async () => {
    const firstFeed = await NativeAdFeed.create({ feedId: 'first-feed', isTesting: true });
    const secondFeed = await NativeAdFeed.create({ feedId: 'second-feed', isTesting: true });
    createdFeeds.push(firstFeed, secondFeed);

    const firstElement = document.createElement('capacitor-admob-native');
    firstElement.setAttribute('feed-id', firstFeed.feedId);
    firstElement.setAttribute('slot-key', 'first-slot');
    document.body.appendChild(firstElement);

    const secondElement = document.createElement('capacitor-admob-native');
    secondElement.setAttribute('feed-id', secondFeed.feedId);
    secondElement.setAttribute('slot-key', 'second-slot');
    document.body.appendChild(secondElement);
    await settle();

    for (const [batch] of bridge.updateNativeAdPlacements.mock.calls) {
      expect(batch.placements.every((placement: { feedId: string }) => placement.feedId === batch.feedId)).toBe(true);
    }
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledWith(expect.objectContaining({ feedId: firstFeed.feedId }));
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledWith(
      expect.objectContaining({ feedId: secondFeed.feedId }),
    );
  });

  it('limits native ad loads even when more attached slots are visible', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'bounded-feed', isTesting: true });
    createdFeeds.push(feed);

    for (let index = 0; index < 4; index += 1) {
      const element = document.createElement('capacitor-admob-native');
      element.setAttribute('feed-id', feed.feedId);
      element.setAttribute('slot-key', `visible-slot-${index}`);
      document.body.appendChild(element);
    }
    await settle();

    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(3);
    await expect(feed.reload('visible-slot-3')).rejects.toThrow('Native ad capacity');
    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(3);
  });

  it('evicts an attached offscreen ad before loading the next visible slot', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'offscreen-feed', isTesting: true });
    createdFeeds.push(feed);
    const elements = Array.from({ length: 3 }, (_, index) => {
      const element = document.createElement('capacitor-admob-native');
      element.setAttribute('feed-id', feed.feedId);
      element.setAttribute('slot-key', `attached-slot-${index}`);
      document.body.appendChild(element);
      return element;
    });
    await settle();

    observers[0].setIntersecting(elements[0], false);
    const nextElement = document.createElement('capacitor-admob-native');
    nextElement.setAttribute('feed-id', feed.feedId);
    nextElement.setAttribute('slot-key', 'attached-slot-3');
    document.body.appendChild(nextElement);
    await settle();

    expect(bridge.removeNativeAd).toHaveBeenCalledWith(
      expect.objectContaining({ feedId: feed.feedId, slotKey: 'attached-slot-0' }),
    );
    expect(bridge.loadNativeAd).toHaveBeenCalledTimes(4);
  });

  it('uses one session for start, placement updates, and destroy', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'session-feed', isTesting: true });
    const startOptions = bridge.startNativeAdFeed.mock.calls[0][0];
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'session-slot');
    document.body.appendChild(element);
    await settle();
    await feed.destroy();

    expect(startOptions).toEqual({ feedId: feed.feedId, sessionId: expect.any(String) });
    expect(bridge.updateNativeAdPlacements).toHaveBeenCalledWith(
      expect.objectContaining({ feedId: feed.feedId, sessionId: startOptions.sessionId }),
    );
    expect(bridge.destroyNativeAdFeed).toHaveBeenCalledWith(startOptions);
  });

  it('hides placements while explicitly paused and restores them after resume', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'paused-feed', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'paused-slot');
    document.body.appendChild(element);
    await settle();

    await feed.pause();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({ placements: [expect.objectContaining({ visible: false })] }),
    );

    document.dispatchEvent(new Event('scroll'));
    await settle();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({ placements: [expect.objectContaining({ visible: false })] }),
    );
    feed.resume();
    await settle();
    expect(bridge.updateNativeAdPlacements).toHaveBeenLastCalledWith(
      expect.objectContaining({ placements: [expect.objectContaining({ visible: true })] }),
    );
  });

  it('keeps a load that finishes during pause hidden', async () => {
    let finishLoad: (() => void) | undefined;
    bridge.loadNativeAd.mockImplementationOnce(
      () =>
        new Promise<void>((resolve) => {
          finishLoad = resolve;
        }),
    );
    const feed = await NativeAdFeed.create({ feedId: 'pause-load-feed', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'pause-load-slot');
    document.body.appendChild(element);
    await vi.advanceTimersByTimeAsync(150);

    await feed.pause();
    finishLoad?.();
    await vi.advanceTimersByTimeAsync(0);

    const batches = bridge.updateNativeAdPlacements.mock.calls.map(([batch]) => batch);
    expect(
      batches.every((batch) => batch.placements.every((placement: { visible: boolean }) => !placement.visible)),
    ).toBe(true);
  });

  it('stays fail-closed when a load fails during pause', async () => {
    let failLoad: ((error: Error) => void) | undefined;
    bridge.loadNativeAd.mockImplementationOnce(
      () =>
        new Promise<void>((_resolve, reject) => {
          failLoad = reject;
        }),
    );
    const feed = await NativeAdFeed.create({ feedId: 'pause-load-error-feed', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'pause-load-error-slot');
    document.body.appendChild(element);
    await vi.advanceTimersByTimeAsync(150);

    await feed.pause();
    failLoad?.(new Error('network'));
    await vi.advanceTimersByTimeAsync(0);

    const batches = bridge.updateNativeAdPlacements.mock.calls.map(([batch]) => batch);
    expect(
      batches.every((batch) => batch.placements.every((placement: { visible: boolean }) => !placement.visible)),
    ).toBe(true);
  });

  it('rejects pause when the native hide update fails', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'pause-error-feed', isTesting: true });
    createdFeeds.push(feed);
    bridge.updateNativeAdPlacements.mockRejectedValueOnce(new Error('bridge unavailable'));

    await expect(feed.pause()).rejects.toThrow('Failed to hide native ad placements');
  });

  it('retries native cleanup after destroy fails', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'destroy-retry-feed', isTesting: true });
    bridge.destroyNativeAdFeed.mockRejectedValueOnce(new Error('bridge unavailable'));

    await expect(feed.destroy()).rejects.toThrow('bridge unavailable');
    await feed.destroy();

    expect(bridge.destroyNativeAdFeed).toHaveBeenCalledTimes(2);
  });

  it('requires the policy-safe medium slot width before loading', async () => {
    vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockReturnValue({
      x: 0,
      y: 0,
      top: 0,
      left: 0,
      right: 143,
      bottom: 300,
      width: 143,
      height: 300,
      toJSON: () => ({}),
    });
    const feed = await NativeAdFeed.create({ feedId: 'medium-size-feed', isTesting: true });
    createdFeeds.push(feed);
    const element = document.createElement('capacitor-admob-native');
    element.setAttribute('feed-id', feed.feedId);
    element.setAttribute('slot-key', 'medium-size-slot');
    document.body.appendChild(element);
    await settle();

    expect(bridge.loadNativeAd).not.toHaveBeenCalled();
  });

  it('preserves author heights and display for custom and ordinary slots', async () => {
    const feed = await NativeAdFeed.create({ feedId: 'styled-feed', isTesting: true });
    createdFeeds.push(feed);
    const style = document.createElement('style');
    style.textContent = '.native-slot { display: none; height: 280px; } .native-slot.zero { height: 0; }';
    document.head.appendChild(style);
    for (const tag of ['capacitor-admob-native', 'div']) {
      for (const zero of [false, true]) {
        const element = document.createElement(tag);
        element.className = zero ? 'native-slot zero' : 'native-slot';
        document.body.appendChild(element);
        feed.attach(`${tag}-${zero}`, element);

        expect(element.style.display).toBe('');
        expect(element.style.height).toBe('');
        expect(getComputedStyle(element).display).toBe('none');
        expect(getComputedStyle(element).height).toBe(zero ? '0px' : '280px');
      }
    }
    style.remove();
  });

  it('cleans up registration when the native platform rejects feed startup', async () => {
    bridge.startNativeAdFeed.mockRejectedValueOnce(new Error('unsupported platform'));
    await expect(NativeAdFeed.create({ feedId: 'startup-error-feed', isTesting: true })).rejects.toThrow(
      'unsupported platform',
    );

    const feed = await NativeAdFeed.create({ feedId: 'startup-error-feed', isTesting: true });
    createdFeeds.push(feed);
    expect(bridge.startNativeAdFeed).toHaveBeenCalledTimes(2);
  });

  it('bounds the number of active feed managers', async () => {
    const firstFeed = await NativeAdFeed.create({ feedId: 'bounded-manager-1', isTesting: true });
    const secondFeed = await NativeAdFeed.create({ feedId: 'bounded-manager-2', isTesting: true });
    createdFeeds.push(firstFeed, secondFeed);

    await expect(NativeAdFeed.create({ feedId: 'bounded-manager-3', isTesting: true })).rejects.toThrow(
      'At most 2 native ad feeds can be active',
    );
  });

  it('rejects native feed startup on the web implementation', async () => {
    const web = new AdMobWeb();
    await expect(web.startNativeAdFeed()).rejects.toThrow('only available on iOS and Android');
  });
});
