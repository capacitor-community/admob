import { Capacitor } from '@capacitor/core';
import type { PluginListenerHandle } from '@capacitor/core';

import type { NativeAdDefinitions } from './native-ad-definitions.interface';
import { connectNativeAdElements, defineNativeAdElement } from './native-ad-element';
import type { NativeAdEvent, NativeAdErrorEvent, NativeAdRevenueEvent } from './native-ad-event.interface';
import { measureNativeAdSlot, measureNativeAdScrollContainer, nativeAdAncestors } from './native-ad-geometry';
import type { MeasuredNativeAdSlot } from './native-ad-geometry';
import type { NativeAdFeedOptions } from './native-ad-options.interface';
import type { NativeAdPlacement } from './native-ad-placement.interface';
import type { NativeAdPluginEvents } from './native-ad-plugin-events.enum';
import { NativeAdTemplate } from './native-ad-template.enum';

const SCROLL_SETTLE_MS = 120;
const SNAPSHOT_IDLE_MS = 50;
const MAX_SNAPSHOT_ATTEMPTS = 3;
const MAX_NATIVE_ADS = 3;
const MAX_ACTIVE_FEEDS = 2;
const MIN_SMALL_SLOT_WIDTH = 120;
const MIN_MEDIUM_SLOT_WIDTH = 144;
const MIN_MEDIUM_SLOT_HEIGHT = 300;
const MIN_SMALL_SLOT_HEIGHT = 120;
const TEMPLATE_ATTRIBUTE = 'data-capacitor-admob-template';

const applySlotDefaults = (element: HTMLElement, template: NativeAdTemplate): void => {
  const root = element.getRootNode();
  const container = root instanceof ShadowRoot ? root : element.ownerDocument.head;
  if (!container.querySelector('style[data-capacitor-admob-defaults]')) {
    const style = element.ownerDocument.createElement('style');
    style.setAttribute('data-capacitor-admob-defaults', '');
    // Prefer the first cascade layer; older WebViews use zero-specificity rules.
    const defaults = `
      :where([${TEMPLATE_ATTRIBUTE}]) { display: block; height: 320px; }
      :where([${TEMPLATE_ATTRIBUTE}="small"]) { height: 120px; }
    `;
    style.textContent =
      'CSSLayerBlockRule' in globalThis ? `@layer capacitor-admob-defaults { ${defaults} }` : defaults;
    container.prepend(style);
  }
  element.setAttribute(TEMPLATE_ATTRIBUTE, template);
};

type SlotStatus = 'idle' | 'loading' | 'loaded' | 'failed' | 'removing' | 'removeFailed';

interface SlotSnapshot {
  image: HTMLImageElement;
  width: number;
  height: number;
  loadVersion: number;
  position: string;
}

interface SlotState {
  slotKey: string;
  element?: HTMLElement;
  generation: number;
  intersecting: boolean;
  status: SlotStatus;
  lastUsed: number;
  loadVersion: number;
  snapshot?: SlotSnapshot;
  pendingSnapshot?: SlotSnapshot;
  usingSnapshot?: boolean;
  snapshotDueAt?: number;
  snapshotAttemptKey?: string;
  snapshotAttempts?: number;
}

let bridge: NativeAdDefinitions | undefined;
let sessionSequence = 0;

const feeds = new Map<string, NativeAdFeed>();

const createSessionId = (): string =>
  globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${++sessionSequence}-${Math.random().toString(36).slice(2)}`;

type NativeAdListener =
  ((event: NativeAdEvent) => void) | ((event: NativeAdErrorEvent) => void) | ((event: NativeAdRevenueEvent) => void);

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
  private readonly nativeScrollElement?: HTMLElement;
  private readonly sessionId = createSessionId();
  private readonly slots = new Map<string, SlotState>();
  private readonly elementSlots = new Map<HTMLElement, string>();
  private readonly scrollRoots = new Set<ShadowRoot>();
  private readonly intersectionObserver?: IntersectionObserver;
  private readonly resizeObserver?: ResizeObserver;
  private snapshotScrolling = false;
  private snapshotEpoch = 0;
  private snapshotTimer?: ReturnType<typeof setTimeout>;
  private snapshotCapturePending = false;
  private destroyed = false;
  private nativeStarted = false;
  private paused = false;
  private layoutInvalidated = false;
  private placementFrame?: number;
  private lastPlacements?: string;
  private lastPlacementWasDirect = false;
  private placementAcknowledgementRequired = false;
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
    if (options.androidScrollSnapshot && (Capacitor.getPlatform() !== 'android' || options.isTesting !== true)) {
      throw new Error('androidScrollSnapshot requires Android test ads');
    }
    this.options = { ...options, feedId: id };
    this.nativeScrollElement = Capacitor.getPlatform() === 'ios' ? options.scrollElement : undefined;

    if (typeof IntersectionObserver !== 'undefined') {
      this.intersectionObserver = new IntersectionObserver((entries) => {
        for (const entry of entries) {
          const slotKey = this.elementSlots.get(entry.target as HTMLElement);
          const state = slotKey ? this.slots.get(slotKey) : undefined;
          if (state) {
            state.intersecting = entry.isIntersecting;
          }
        }
        this.schedulePlacementUpdate();
        this.scheduleEvaluation();
      });
    }
    if (typeof ResizeObserver !== 'undefined') {
      this.resizeObserver = new ResizeObserver(this.handleViewportMotion);
      if (this.nativeScrollElement) this.resizeObserver.observe(this.nativeScrollElement);
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
    if (options.isTesting !== true && !options.adId?.trim()) {
      throw new Error('adId is required unless isTesting is true');
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
    applySlotDefaults(element, this.options.template ?? NativeAdTemplate.Medium);
    state.element = element;
    state.generation += 1;
    state.lastUsed = Date.now();
    this.slots.set(normalizedKey, state);
    this.elementSlots.set(element, normalizedKey);
    this.updateScrollRoots();
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
      this.clearSnapshot(state);
      state.element = undefined;
      state.intersecting = false;
      state.generation += 1;
      state.lastUsed = Date.now();
    }
    this.intersectionObserver?.unobserve(element);
    this.resizeObserver?.unobserve(element);
    this.elementSlots.delete(element);
    this.updateScrollRoots();
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
      await this.requestPlacementUpdate();
      this.scheduleSnapshots();
    });
  }

  /** Immediately hides this feed's overlays until {@link resume} is called. */
  async pause(): Promise<void> {
    this.assertActive();
    this.paused = true;
    this.resetSnapshots();
    this.layoutInvalidated = true;
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
    this.invalidateSnapshots();
    this.layoutInvalidated = true;
    this.scheduleEvaluation();
    if (!(await this.requestPlacementUpdate())) {
      throw new Error('Failed to hide native ad placements');
    }
  }

  async destroy(): Promise<void> {
    if (!this.destroyed) {
      this.destroyed = true;
      this.resetSnapshots();
      feeds.delete(this.options.feedId);
      document.removeEventListener('scroll', this.handleViewportMotion, true);
      for (const root of this.scrollRoots) {
        root.removeEventListener('scroll', this.handleViewportMotion, true);
      }
      this.scrollRoots.clear();
      document.removeEventListener('touchmove', this.handleViewportMotion, true);
      document.removeEventListener('wheel', this.handleViewportMotion, true);
      window.removeEventListener('resize', this.handleViewportMotion);
      window.visualViewport?.removeEventListener('resize', this.handleViewportMotion);
      document.removeEventListener('visibilitychange', this.handleVisibilityChange);
      document.removeEventListener('load', this.handleViewportMotion, true);
      if (this.settleTimer) {
        clearTimeout(this.settleTimer);
      }
      if (this.placementFrame !== undefined) {
        cancelAnimationFrame(this.placementFrame);
        this.placementFrame = undefined;
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

  private readonly handleViewportMotion = (event?: Event | ResizeObserverEntry[]): void => {
    if (this.destroyed) {
      return;
    }
    if (
      event instanceof Event &&
      event.type === 'load' &&
      event.target instanceof HTMLImageElement &&
      event.target.dataset['admobSnapshot'] !== undefined
    )
      return;
    if (this.options.androidScrollSnapshot) {
      if (event instanceof Event && ['scroll', 'touchmove', 'wheel'].includes(event.type)) {
        this.beginSnapshotScroll();
      } else {
        this.invalidateSnapshots();
      }
    }
    if (!(
      this.nativeScrollElement &&
      event instanceof Event &&
      ['scroll', 'touchmove', 'wheel'].includes(event.type)
    )) {
      this.schedulePlacementUpdate();
    }
    this.scheduleEvaluation();
  };

  private schedulePlacementUpdate(): void {
    if (this.destroyed || this.placementFrame !== undefined) {
      return;
    }
    this.placementFrame = requestAnimationFrame(() => {
      this.placementFrame = undefined;
      void this.requestPlacementUpdate(true);
    });
  }

  private updateScrollRoots(): void {
    const roots = new Set<ShadowRoot>();
    for (const element of this.elementSlots.keys()) {
      for (const ancestor of [element, ...nativeAdAncestors(element)]) {
        const root = ancestor.getRootNode();
        if (root instanceof ShadowRoot) roots.add(root);
      }
    }
    for (const root of this.scrollRoots) {
      if (!roots.has(root)) {
        root.removeEventListener('scroll', this.handleViewportMotion, true);
        this.scrollRoots.delete(root);
      }
    }
    for (const root of roots) {
      if (!this.scrollRoots.has(root)) {
        // Element scroll events do not cross a shadow boundary.
        root.addEventListener('scroll', this.handleViewportMotion, true);
        this.scrollRoots.add(root);
      }
    }
  }

  private readonly handleVisibilityChange = (): void => {
    if (this.destroyed) {
      return;
    }
    if (document.hidden) {
      this.resetSnapshots();
      this.layoutInvalidated = true;
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
    const epoch = this.snapshotEpoch;
    this.settleTimer = setTimeout(() => {
      this.layoutInvalidated = false;
      void this.runOperation(async () => {
        if (this.options.androidScrollSnapshot && epoch !== this.snapshotEpoch) return;
        await this.evaluate();
      });
    }, SCROLL_SETTLE_MS);
  }

  private async evaluate(): Promise<void> {
    if (this.destroyed || this.paused || document.hidden) {
      return;
    }

    if (this.options.androidScrollSnapshot) {
      this.snapshotScrolling = false;
      for (const state of this.slots.values()) state.usingSnapshot = false;
      // A native acknowledgement is not a draw fence. Keep the underlay during and after restoration.
      await this.requestPlacementUpdate();
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
    await this.requestPlacementUpdate();
    this.scheduleSnapshots();
  }

  private beginSnapshotScroll(): void {
    ++this.snapshotEpoch;
    if (this.snapshotTimer) clearTimeout(this.snapshotTimer);
    if (this.paused || document.hidden || this.snapshotScrolling) return;
    this.snapshotScrolling = true;
    let hasSnapshot = false;
    for (const state of this.slots.values()) {
      // Only images whose DOM insertion passed the native visual fence can hide a native view.
      state.usingSnapshot = Boolean(state.element && state.snapshot?.loadVersion === state.loadVersion);
      hasSnapshot ||= state.usingSnapshot;
    }
    if (hasSnapshot) void this.requestPlacementUpdate(true);
  }

  private needsSnapshot(state: SlotState): boolean {
    if (!state.element || state.status !== 'loaded') return false;
    const rect = state.element.getBoundingClientRect();
    const key = `${state.loadVersion}:${rect.width}:${rect.height}`;
    if (state.snapshotAttemptKey !== key) {
      state.snapshotAttemptKey = key;
      state.snapshotAttempts = 0;
    }
    return (
      (state.snapshotAttempts ?? 0) < MAX_SNAPSHOT_ATTEMPTS &&
      (!state.snapshot ||
        state.snapshot.loadVersion !== state.loadVersion ||
        Math.abs(state.snapshot.width - rect.width) > 1 ||
        Math.abs(state.snapshot.height - rect.height) > 1)
    );
  }

  private scheduleSnapshots(): void {
    if (
      !this.options.androidScrollSnapshot ||
      this.destroyed ||
      this.paused ||
      this.snapshotScrolling ||
      document.hidden ||
      this.snapshotCapturePending ||
      !bridge?.captureNativeAdSnapshot ||
      !bridge.prepareNativeAdSnapshot
    )
      return;
    if (this.snapshotTimer) clearTimeout(this.snapshotTimer);
    const states = this.getVisibleStates().filter((state) => this.needsSnapshot(state));
    if (!states.length) return;
    for (const state of states) state.snapshotDueAt ??= Date.now() + SNAPSHOT_IDLE_MS;
    const delay = Math.max(SNAPSHOT_IDLE_MS, Math.min(...states.map((state) => state.snapshotDueAt!)) - Date.now());
    this.snapshotTimer = setTimeout(() => void this.captureSnapshots(), delay);
  }

  private async captureSnapshots(): Promise<void> {
    if (this.snapshotCapturePending || this.snapshotScrolling || this.destroyed || this.paused || document.hidden)
      return;
    this.snapshotCapturePending = true;
    const epoch = this.snapshotEpoch;
    try {
      for (const state of this.getVisibleStates()) {
        if (epoch !== this.snapshotEpoch) break;
        const element = state.element;
        if (
          !element ||
          !this.needsSnapshot(state) ||
          (state.snapshotDueAt ?? 0) > Date.now() ||
          !bridge?.captureNativeAdSnapshot ||
          !bridge.prepareNativeAdSnapshot
        )
          continue;
        const version = state.loadVersion;
        const generation = state.generation;
        const rect = element.getBoundingClientRect();
        const current = () =>
          epoch === this.snapshotEpoch &&
          !this.destroyed &&
          !this.paused &&
          !document.hidden &&
          state.element === element &&
          state.loadVersion === version &&
          state.generation === generation &&
          state.status === 'loaded';
        let candidate: SlotSnapshot | undefined;
        state.snapshotDueAt = Date.now() + 1000;
        try {
          const { dataUrl } = await bridge.captureNativeAdSnapshot({
            feedId: this.feedId,
            sessionId: this.sessionId,
            slotKey: state.slotKey,
          });
          if (!current()) continue;
          const image = new Image();
          image.dataset['admobSnapshot'] = '';
          image.src = dataUrl;
          await image.decode();
          if (!current()) continue;
          const position = state.snapshot?.position ?? element.style.position;
          if (getComputedStyle(element).position === 'static') element.style.position = 'relative';
          image.alt = '';
          image.setAttribute('aria-hidden', 'true');
          image.draggable = false;
          candidate = { image, position, width: rect.width, height: rect.height, loadVersion: version };
          state.pendingSnapshot = candidate;
          this.updateSnapshotGeometry(state, candidate);
          // Both buffers remain drawn until the new image is ready for the next WebView draw.
          element.append(image);
          await bridge.prepareNativeAdSnapshot({
            feedId: this.feedId,
            sessionId: this.sessionId,
            slotKey: state.slotKey,
          });
          if (!current() || state.pendingSnapshot !== candidate) continue;
          state.snapshot?.image.remove();
          state.snapshot = candidate;
          state.pendingSnapshot = undefined;
          state.snapshotDueAt = undefined;
        } catch {
          if (current()) state.snapshotAttempts = (state.snapshotAttempts ?? 0) + 1;
          // Keep the old image on failure; bounded retries avoid continuous CPU/memory churn.
        } finally {
          if (candidate && state.snapshot !== candidate) {
            candidate.image.remove();
            if (state.pendingSnapshot === candidate) {
              state.pendingSnapshot = undefined;
              if (!state.snapshot && state.element === element) element.style.position = candidate.position;
            }
          }
        }
        if (epoch !== this.snapshotEpoch) break;
      }
    } finally {
      this.snapshotCapturePending = false;
      this.scheduleSnapshots();
    }
  }

  private updateSnapshotGeometry(state: SlotState, snapshot: SlotSnapshot): void {
    if (!state.element) return;
    const style = getComputedStyle(state.element);
    const left = Number.parseFloat(style.borderLeftWidth) || 0;
    const top = Number.parseFloat(style.borderTopWidth) || 0;
    const right = Number.parseFloat(style.borderRightWidth) || 0;
    const bottom = Number.parseFloat(style.borderBottomWidth) || 0;
    snapshot.image.style.cssText = `position:absolute;left:${-left}px;top:${-top}px;
      width:calc(100% + ${left + right}px);height:calc(100% + ${top + bottom}px);box-sizing:border-box;
      border:0;padding:0;margin:0;min-width:0;min-height:0;max-width:none;max-height:none;pointer-events:none;display:block;`;
  }

  private clearSnapshot(state: SlotState): void {
    const snapshot = state.snapshot ?? state.pendingSnapshot;
    state.snapshot?.image.remove();
    state.pendingSnapshot?.image.remove();
    if (snapshot && state.element) state.element.style.position = snapshot.position;
    state.snapshot = undefined;
    state.pendingSnapshot = undefined;
    state.usingSnapshot = false;
    state.snapshotAttempts = 0;
    state.snapshotDueAt = undefined;
  }

  private invalidateSnapshots(): void {
    // Keep the last drawn image through layout invalidation instead of creating an empty slot.
    ++this.snapshotEpoch;
    if (this.snapshotTimer) clearTimeout(this.snapshotTimer);
    for (const state of this.slots.values()) {
      if (state.snapshot) this.updateSnapshotGeometry(state, state.snapshot);
      if (state.pendingSnapshot) this.updateSnapshotGeometry(state, state.pendingSnapshot);
    }
  }

  private resetSnapshots(): void {
    ++this.snapshotEpoch;
    this.snapshotScrolling = false;
    if (this.snapshotTimer) clearTimeout(this.snapshotTimer);
    for (const state of this.slots.values()) this.clearSnapshot(state);
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
        adId: this.options.adId,
        isTesting: this.options.isTesting,
        npa: this.options.npa,
      });
      if (state.loadVersion === loadVersion) {
        state.status = 'loaded';
        state.lastUsed = Date.now();
        state.snapshotDueAt = Date.now() + SNAPSHOT_IDLE_MS;
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
    this.clearSnapshot(state);
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

  private requestPlacementUpdate(allowDirect = false): Promise<boolean> {
    this.updateDirty = true;
    this.placementAcknowledgementRequired ||= !allowDirect;
    if (!this.placementUpdateTask) {
      this.placementUpdateTask = Promise.resolve().then(() => this.flushPlacementUpdates());
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
        const allowDirect = !this.placementAcknowledgementRequired;
        this.placementAcknowledgementRequired = false;
        const placements = [...this.slots.values()]
          .filter((state) => state.status === 'loaded' && !state.usingSnapshot)
          .map((state): NativeAdPlacement => {
            const measured = state.element ? measureNativeAdSlot(state.element, this.nativeScrollElement) : undefined;
            const visible = Boolean(
              !this.paused &&
              (!this.layoutInvalidated || (this.options.androidScrollSnapshot && !state.snapshot)) &&
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
        const scrollContainer = this.nativeScrollElement
          ? measureNativeAdScrollContainer(this.nativeScrollElement)
          : undefined;
        const serializedPlacements = JSON.stringify({ placements, scrollContainer });
        if (serializedPlacements === this.lastPlacements && (allowDirect || !this.lastPlacementWasDirect)) {
          continue;
        }
        try {
          const batch = {
            feedId: this.options.feedId,
            sessionId: this.sessionId,
            sequence: ++this.placementSequence,
            placements,
            ...(scrollContainer ? { scrollContainer } : {}),
          };
          const channel =
            Capacitor.getPlatform() === 'android'
              ? (window as Window & { capacitorAdMobPlacements?: { postMessage(message: string): void } })
                  .capacitorAdMobPlacements
              : undefined;
          let sentDirectly = false;
          if (allowDirect && channel) {
            try {
              channel.postMessage(JSON.stringify(batch));
              sentDirectly = true;
            } catch {
              // Keep the acknowledged bridge available if the direct channel fails.
            }
          }
          if (!sentDirectly) await bridge.updateNativeAdPlacements(batch);
          this.lastPlacementWasDirect = sentDirectly;
          this.lastPlacements = serializedPlacements;
        } catch {
          this.lastPlacements = undefined;
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
