import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideIonicAngular } from '@ionic/angular';
import { NativeAdFeed, NativeAdPluginEvents } from '@capacitor-community/admob';
import { Capacitor } from '@capacitor/core';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { NativeAdDemo } from './native-ad-demo';

describe('NativeAdDemo', () => {
  let fixture: ComponentFixture<NativeAdDemo>;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideIonicAngular()] });
    fixture = TestBed.createComponent(NativeAdDemo);
  });

  afterEach(() => vi.restoreAllMocks());

  it('uses stable logical keys for each native ad row', () => {
    expect(fixture.componentInstance.vm.feedItems.filter((item) => item.kind === 'native-ad')).toEqual([
      expect.objectContaining({ id: 'ad-after-capacitor', slotKey: 'sponsored-after-capacitor' }),
      expect.objectContaining({ id: 'ad-after-ionic', slotKey: 'sponsored-after-ionic' }),
    ]);
  });

  it('does not invalidate ad layout when the diagnostic log updates', async () => {
    vi.spyOn(Capacitor, 'isNativePlatform').mockReturnValue(true);
    fixture.destroy();
    fixture = TestBed.createComponent(NativeAdDemo);
    const feed = {
      invalidateLayout: vi.fn().mockResolvedValue(undefined),
      addListener: vi.fn().mockResolvedValue(undefined),
      destroy: vi.fn().mockResolvedValue(undefined),
    };
    vi.spyOn(NativeAdFeed, 'create').mockResolvedValue(feed as unknown as NativeAdFeed);
    const vm = fixture.componentInstance.vm;
    await vm.enter();
    fixture.detectChanges();
    await fixture.whenStable();
    feed.invalidateLayout.mockClear();

    vm.lastEvent.set({ name: NativeAdPluginEvents.Loaded, value: { slotKey: 'test-ad' } });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(feed.invalidateLayout).not.toHaveBeenCalled();
    await vm.leave();
  });

  it('discards a late feed after leaving and destroys the next feed without waiting for listener setup', async () => {
    vi.spyOn(Capacitor, 'isNativePlatform').mockReturnValue(true);
    fixture.destroy();
    fixture = TestBed.createComponent(NativeAdDemo);
    const vm = fixture.componentInstance.vm;
    let finishCreate!: (feed: NativeAdFeed) => void;
    let finishListeners!: () => void;
    const oldFeed = { destroy: vi.fn().mockResolvedValue(undefined) };
    const newFeed = {
      destroy: vi.fn().mockResolvedValue(undefined),
      addListener: vi.fn().mockReturnValue(
        new Promise<void>((resolve) => {
          finishListeners = resolve;
        }),
      ),
    };
    const create = vi
      .spyOn(NativeAdFeed, 'create')
      .mockImplementationOnce(
        () =>
          new Promise<NativeAdFeed>((resolve) => {
            finishCreate = resolve;
          }),
      )
      .mockResolvedValueOnce(newFeed as unknown as NativeAdFeed);

    const firstEntry = vm.enter();
    await vi.waitFor(() => expect(create).toHaveBeenCalledTimes(1));
    await vm.leave();
    const secondEntry = vm.enter();
    finishCreate(oldFeed as unknown as NativeAdFeed);
    await firstEntry;
    await vi.waitFor(() => expect(newFeed.addListener).toHaveBeenCalled());
    expect(oldFeed.destroy).toHaveBeenCalledOnce();
    expect(newFeed.destroy).not.toHaveBeenCalled();

    await vm.leave();
    expect(newFeed.destroy).toHaveBeenCalledOnce();
    finishListeners();
    await secondEntry;
  });
});
