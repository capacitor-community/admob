import type { PluginListenerHandle } from '@capacitor/core';

import type { NativeAdDefinitions } from './native-ad-definitions.interface';
import { connectNativeAdElements, defineNativeAdElement } from './native-ad-element';
import type { NativeAdEvent, NativeAdErrorEvent, NativeAdRevenueEvent } from './native-ad-event.interface';
import { measureNativeAdSlot } from './native-ad-geometry';
import type { MeasuredNativeAdSlot } from './native-ad-geometry';
import type { NativeAdFeedOptions } from './native-ad-options.interface';
import type { NativeAdPlacement } from './native-ad-placement.interface';
import type { NativeAdPluginEvents } from './native-ad-plugin-events.enum';
import { NativeAdTemplate } from './native-ad-template.enum';

const SCROLL_SETTLE_MS = 120;
const MAX_NATIVE_ADS = 3;
const MAX_ACTIVE_FEEDS = 2;
const MIN_SMALL_SLOT_WIDTH = 120;
const MIN_MEDIUM_SLOT_WIDTH = 144;
const MIN_MEDIUM_SLOT_HEIGHT = 300;
const MIN_SMALL_SLOT_HEIGHT = 120;

type SlotStatus = 'idle' | 'loading' | 'loaded' | 'failed' | 'removing' | 'removeFailed';

interface SlotState {
  slotKey: string;
  element?: HTMLElement;
  generation: number;
  intersecting: boolean;
  status: SlotStatus;
  lastUsed: number;
  loadVersion: number;
}

let bridge: NativeAdDefinitions | undefined;
let sessionSequence = 0;

const feeds = new Map<string, NativeAdFeed>();

const createSessionId = (): string =>
  globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${++sessionSequence}-${Math.random().toString(36).slice(2)}`;

type NativeAdListener =
  | ((event: NativeAdEvent) => void)
  | ((event: NativeAdErrorEvent) => void)
  | ((event: NativeAdRevenueEvent) => void);

/**
 * Experimental manager for native-ad integration research.
 *
 * A stable `slotKey`, rather than the element or array index, owns the native
 * ad lifecycle. Virtual-scroller compatibility has not yet been validated.
 *
 * @experimental Native overlay gesture routing is still being validated on devices.
 */
export class NativeAdFeed {
  private readonly options: NativeAdFeedOptions;
  private readonly sessionId = createSessionId();
  private readonly slots = new Map<string, SlotState>();
  private readonly elementSlots = new Map<HTMLElement, string>();
  private readonly intersectionObserver?: IntersectionObserver;
  private readonly resizeObserver?: ResizeObserver;
  private destroyed = false;
  private nativeStarted = false;
  private paused = false;
  private scrolling = false;
  private settleTimer?: ReturnType<typeof setTimeout>;
  private placementUpdateTask?: Promise<boolean>;
  private operationTask: Promise<void> = Promise.resolve();
  private destroyTask?: Promise<void>;
  private updateDirty = false;
  private placementSequence = 0;
  private readonly listenerHandles = new Set<PluginListenerHandle>();

  private constructor(options: NativeAdFeedOptions) {
    const id = options.feedId.trim();
    if (!id) {
      throw new Error('Native ad feed id must not be empty');
    }
    if (feeds.has(id)) {
      throw new Error(`Native ad feed id already exists: ${id}`);
    }
    if (feeds.size >= MAX_ACTIVE_FEEDS) {
      throw new Error(`At most ${MAX_ACTIVE_FEEDS} native ad feeds can be active`);
    }
    this.options = { ...options, feedId: id };

    if (typeof IntersectionObserver !== 'undefined') {
      this.intersectionObserver = new IntersectionObserver((entries) => {
        for (const entry of entries) {
          const slotKey = this.elementSlots.get(entry.target as HTMLElement);
          const state = slotKey ? this.slots.get(slotKey) : undefined;
          if (state) {
            state.intersecting = entry.isIntersecting;
          }
        }
        this.scheduleEvaluation();
      });
    }
    if (typeof ResizeObserver !== 'undefined') {
      this.resizeObserver = new ResizeObserver(this.handleViewportMotion);
    }

    document.addEventListener('scroll', this.handleViewportMotion, true);
    document.addEventListener('touchmove', this.handleViewportMotion, { capture: true, passive: true });
    document.addEventListener('wheel', this.handleViewportMotion, { capture: true, passive: true });
    window.addEventListener('resize', this.handleViewportMotion, { passive: true });
    window.visualViewport?.addEventListener('resize', this.handleViewportMotion, { passive: true });
    document.addEventListener('visibilitychange', this.handleVisibilityChange);
    document.addEventListener('load', this.handleViewportMotion, true);
    feeds.set(id, this);
  }

  static async create(options: NativeAdFeedOptions): Promise<NativeAdFeed> {
    if (!bridge) {
      throw new Error('AdMob native ad bridge is not configured');
    }
    if (typeof document === 'undefined' || typeof window === 'undefined') {
      throw new Error('NativeAdFeed can only be created in a browser environment');
    }
    if (options.isTesting !== true) {
      throw new Error('Native ads are restricted to test ads during the research preview');
    }
    const feed = new NativeAdFeed(options);
    try {
      await bridge.startNativeAdFeed({ feedId: feed.feedId, sessionId: feed.sessionId });
      feed.nativeStarted = true;
      defineNativeAdElement((feedId) => feeds.get(feedId));
      connectNativeAdElements();
      return feed;
    } catch (error) {
      await feed.destroy();
      throw error;
    }
  }

  get feedId(): string {
    return this.options.feedId;
  }

  async addListener(
    eventName:
      | NativeAdPluginEvents.Loaded
      | NativeAdPluginEvents.Clicked
      | NativeAdPluginEvents.AdImpression
      | NativeAdPluginEvents.Opened
      | NativeAdPluginEvents.Closed,
    listener: (event: NativeAdEvent) => void,
  ): Promise<PluginListenerHandle>;
  async addListener(
    eventName: NativeAdPluginEvents.FailedToLoad,
    listener: (event: NativeAdErrorEvent) => void,
  ): Promise<PluginListenerHandle>;
  async addListener(
    eventName: NativeAdPluginEvents.AdPaid,
    listener: (event: NativeAdRevenueEvent) => void,
  ): Promise<PluginListenerHandle>;
  async addListener(eventName: NativeAdPluginEvents, listener: NativeAdListener): Promise<PluginListenerHandle> {
    this.assertActive();
    if (!bridge) {
      throw new Error('AdMob native ad bridge is not configured');
    }
    const nativeHandle = await bridge.addListener(eventName, (event) => {
      if (event.feedId === this.options.feedId && event.sessionId === this.sessionId) {
        const filteredListener = listener as (
          filteredEvent: NativeAdEvent | NativeAdErrorEvent | NativeAdRevenueEvent,
        ) => void;
        filteredListener(event);
      }
    });
    if (this.destroyed) {
      await nativeHandle.remove();
      this.assertActive();
    }
    let removed = false;
    const handle: PluginListenerHandle = {
      remove: async () => {
        if (removed) {
          return;
        }
        removed = true;
        this.listenerHandles.delete(handle);
        await nativeHandle.remove();
      },
    };
    this.listenerHandles.add(handle);
    return handle;
  }

  attach(slotKey: string, element: HTMLElement): void {
    this.assertActive();
    const normalizedKey = slotKey.trim();
    if (!normalizedKey) {
      throw new Error('Native ad slotKey must not be empty');
    }

    const previousKey = this.elementSlots.get(element);
    if (previousKey === normalizedKey) {
      return;
    }
    if (previousKey && previousKey !== normalizedKey) {
      this.detach(element);
    }

    const existing = this.slots.get(normalizedKey);
    if (existing?.element && existing.element !== element) {
      throw new Error(`Native ad slotKey is already attached: ${normalizedKey}`);
    }

    const state: SlotState = existing ?? {
      slotKey: normalizedKey,
      generation: 0,
      intersecting: !this.intersectionObserver,
      status: 'idle',
      lastUsed: Date.now(),
      loadVersion: 0,
    };
    const computedStyle = window.getComputedStyle(element);
    if (computedStyle.display === 'inline') {
      element.style.display = 'block';
    }
    if (computedStyle.height === 'auto') {
      element.style.height =
        (this.options.template ?? NativeAdTemplate.Medium) === NativeAdTemplate.Small ? '120px' : '320px';
    }
    state.element = element;
    state.generation += 1;
    state.lastUsed = Date.now();
    this.slots.set(normalizedKey, state);
    this.elementSlots.set(element, normalizedKey);
    this.intersectionObserver?.observe(element);
    this.resizeObserver?.observe(element);
    this.scheduleEvaluation();
  }

  detach(element: HTMLElement): void {
    const slotKey = this.elementSlots.get(element);
    if (!slotKey) {
      return;
    }
    const state = this.slots.get(slotKey);
    if (state?.element === element) {
      state.element = undefined;
      state.intersecting = false;
      state.generation += 1;
      state.lastUsed = Date.now();
    }
    this.intersectionObserver?.unobserve(element);
    this.resizeObserver?.unobserve(element);
    this.elementSlots.delete(element);
    void this.requestPlacementUpdate();
    this.scheduleEvaluation();
  }

  async reload(slotKey: string): Promise<void> {
    this.assertActive();
    const normalizedKey = slotKey.trim();
    const state = this.slots.get(normalizedKey);
    if (!state) {
      throw new Error(`Native ad slotKey is not registered: ${normalizedKey}`);
    }
    await this.runOperation(async () => {
      await this.removeStateAd(state);
      const protectedStates = new Set(this.getVisibleStates());
      protectedStates.add(state);
      await this.evictUnusedAds(protectedStates);
      if ([...this.slots.values()].filter((slot) => this.occupiesNativeAd(slot)).length >= MAX_NATIVE_ADS) {
        throw new Error('Native ad capacity is currently in use by visible slots');
      }
      await this.loadState(state);
    });
  }

  /** Immediately hides this feed's overlays until {@link resume} is called. */
  async pause(): Promise<void> {
    this.assertActive();
    this.paused = true;
    this.scrolling = true;
    if (!(await this.requestPlacementUpdate())) {
      throw new Error('Failed to hide native ad placements');
    }
  }

  /** Re-evaluates slots and restores eligible overlays after the viewport settles. */
  resume(): void {
    this.assertActive();
    this.paused = false;
    this.scheduleEvaluation();
  }

  /** Hides stale overlays and remeasures after an application-driven layout change. */
  async invalidateLayout(): Promise<void> {
    this.assertActive();
    this.scrolling = true;
    this.scheduleEvaluation();
    if (!(await this.requestPlacementUpdate())) {
      throw new Error('Failed to hide native ad placements');
    }
  }

  async destroy(): Promise<void> {
    if (!this.destroyed) {
      this.destroyed = true;
      feeds.delete(this.options.feedId);
      document.removeEventListener('scroll', this.handleViewportMotion, true);
      document.removeEventListener('touchmove', this.handleViewportMotion, true);
      document.removeEventListener('wheel', this.handleViewportMotion, true);
      window.removeEventListener('resize', this.handleViewportMotion);
      window.visualViewport?.removeEventListener('resize', this.handleViewportMotion);
      document.removeEventListener('visibilitychange', this.handleVisibilityChange);
      document.removeEventListener('load', this.handleViewportMotion, true);
      if (this.settleTimer) {
        clearTimeout(this.settleTimer);
      }
      this.intersectionObserver?.disconnect();
      this.resizeObserver?.disconnect();
    }
    if (this.destroyTask) {
      return this.destroyTask;
    }
    this.destroyTask = this.destroyResources();
    try {
      await this.destroyTask;
    } finally {
      this.destroyTask = undefined;
    }
  }

  private readonly handleViewportMotion = (): void => {
    if (this.destroyed) {
      return;
    }
    this.scrolling = true;
    void this.requestPlacementUpdate();
    this.scheduleEvaluation();
  };

  private readonly handleVisibilityChange = (): void => {
    if (this.destroyed) {
      return;
    }
    if (document.hidden) {
      this.scrolling = true;
      void this.requestPlacementUpdate();
      return;
    }
    this.scheduleEvaluation();
  };

  private scheduleEvaluation(): void {
    if (this.destroyed) {
      return;
    }
    if (this.settleTimer) {
      clearTimeout(this.settleTimer);
    }
    this.settleTimer = setTimeout(() => {
      this.scrolling = false;
      void this.runOperation(() => this.evaluate());
    }, SCROLL_SETTLE_MS);
  }

  private async evaluate(): Promise<void> {
    if (this.destroyed || this.paused || document.hidden) {
      return;
    }

    const visibleStates = this.getVisibleStates();
    for (const state of visibleStates) {
      state.lastUsed = Date.now();
    }
    await this.evictUnusedAds(new Set(visibleStates));
    const occupied = [...this.slots.values()].filter((state) => this.occupiesNativeAd(state)).length;
    const loadable = visibleStates
      .filter((state) => state.status === 'idle')
      .slice(0, Math.max(0, MAX_NATIVE_ADS - occupied));
    await Promise.allSettled(loadable.map((state) => this.loadState(state)));
    void this.requestPlacementUpdate();
  }

  private async loadState(state: SlotState): Promise<void> {
    if (state.status !== 'idle' || !bridge) {
      return;
    }
    state.status = 'loading';
    const loadVersion = ++state.loadVersion;
    try {
      await bridge.loadNativeAd({
        feedId: this.options.feedId,
        sessionId: this.sessionId,
        slotKey: state.slotKey,
        template: this.options.template ?? NativeAdTemplate.Medium,
        style: this.options.style,
        isTesting: true,
        npa: this.options.npa,
      });
      if (state.loadVersion === loadVersion) {
        state.status = 'loaded';
        state.lastUsed = Date.now();
      }
    } catch (error) {
      if (state.loadVersion === loadVersion) {
        state.status = 'failed';
      }
      throw error;
    }
  }

  private async evictUnusedAds(visibleStates: Set<SlotState>): Promise<void> {
    const occupied = [...this.slots.values()].filter((state) => this.occupiesNativeAd(state));
    const requestedLoads = [...visibleStates].filter((state) => state.status === 'idle').length;
    const evictionCount = Math.max(0, occupied.length + Math.min(requestedLoads, MAX_NATIVE_ADS) - MAX_NATIVE_ADS);
    const evicted = occupied
      .filter((state) => !visibleStates.has(state))
      .sort((left, right) => left.lastUsed - right.lastUsed)
      .slice(0, evictionCount);
    await Promise.allSettled(evicted.map((state) => this.removeStateAd(state)));

    const detached = [...this.slots.values()]
      .filter((state) => !state.element && !this.occupiesNativeAd(state))
      .sort((left, right) => right.lastUsed - left.lastUsed);
    for (const state of detached.slice(MAX_NATIVE_ADS)) {
      this.slots.delete(state.slotKey);
    }
  }

  private occupiesNativeAd(state: SlotState): boolean {
    return ['loading', 'loaded', 'removing', 'removeFailed'].includes(state.status);
  }

  private getVisibleStates(): SlotState[] {
    return [...this.slots.values()].filter((state) => {
      const measured = state.element && state.intersecting ? measureNativeAdSlot(state.element) : undefined;
      return Boolean(measured && this.isSupportedSize(measured));
    });
  }

  private isSupportedSize(measured: MeasuredNativeAdSlot): boolean {
    const minimumHeight =
      (this.options.template ?? NativeAdTemplate.Medium) === NativeAdTemplate.Small
        ? MIN_SMALL_SLOT_HEIGHT
        : MIN_MEDIUM_SLOT_HEIGHT;
    const minimumWidth =
      (this.options.template ?? NativeAdTemplate.Medium) === NativeAdTemplate.Small
        ? MIN_SMALL_SLOT_WIDTH
        : MIN_MEDIUM_SLOT_WIDTH;
    return measured.rect.width >= minimumWidth && measured.rect.height >= minimumHeight;
  }

  private async removeStateAd(state: SlotState): Promise<void> {
    if (state.status === 'removing') {
      return;
    }
    if (!bridge || state.status === 'idle' || state.status === 'failed') {
      state.status = 'idle';
      return;
    }
    state.status = 'removing';
    const removeVersion = ++state.loadVersion;
    await this.requestPlacementUpdate();
    try {
      await bridge.removeNativeAd({
        feedId: this.options.feedId,
        sessionId: this.sessionId,
        slotKey: state.slotKey,
      });
      if (state.loadVersion === removeVersion) {
        state.status = 'idle';
      }
    } catch (error) {
      if (state.loadVersion === removeVersion) {
        state.status = 'removeFailed';
      }
      throw error;
    }
  }

  private runOperation(operation: () => Promise<void>): Promise<void> {
    const result = this.operationTask.then(operation);
    this.operationTask = result.catch(() => undefined);
    return result;
  }

  private async destroyResources(): Promise<void> {
    await Promise.all([
      ...[...this.listenerHandles].map((handle) => handle.remove()),
      ...(this.nativeStarted && bridge
        ? [bridge.destroyNativeAdFeed({ feedId: this.options.feedId, sessionId: this.sessionId })]
        : []),
    ]);
    this.nativeStarted = false;
    this.listenerHandles.clear();
    this.slots.clear();
    this.elementSlots.clear();
  }

  private requestPlacementUpdate(): Promise<boolean> {
    this.updateDirty = true;
    if (!this.placementUpdateTask) {
      this.placementUpdateTask = this.flushPlacementUpdates();
    }
    return this.placementUpdateTask;
  }

  private async flushPlacementUpdates(): Promise<boolean> {
    if (!bridge || this.destroyed) {
      return true;
    }
    try {
      while (this.updateDirty && !this.destroyed) {
        this.updateDirty = false;
        const placements = [...this.slots.values()]
          .filter((state) => state.status === 'loaded')
          .map((state): NativeAdPlacement => {
            const measured = state.element ? measureNativeAdSlot(state.element) : undefined;
            const visible = Boolean(
              !this.paused &&
              !this.scrolling &&
              state.intersecting &&
              measured &&
              this.isSupportedSize(measured) &&
              !document.hidden,
            );
            return {
              feedId: this.options.feedId,
              slotKey: state.slotKey,
              generation: state.generation,
              visible,
              rect: measured?.rect,
              clipRect: measured?.clipRect,
            };
          });
        try {
          await bridge.updateNativeAdPlacements({
            feedId: this.options.feedId,
            sessionId: this.sessionId,
            sequence: ++this.placementSequence,
            placements,
          });
        } catch {
          // Retry on the next scroll, resize, or lifecycle update without
          // creating an unhandled rejection from this fire-and-forget task.
          this.updateDirty = true;
          return false;
        }
      }
      return true;
    } catch {
      this.updateDirty = true;
      return false;
    } finally {
      this.placementUpdateTask = undefined;
    }
  }

  private assertActive(): void {
    if (this.destroyed) {
      throw new Error(`Native ad feed has been destroyed: ${this.options.feedId}`);
    }
  }
}

export const configureNativeAdBridge = (adMobBridge: NativeAdDefinitions): void => {
  bridge = adMobBridge;
};
