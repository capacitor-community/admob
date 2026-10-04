import { TestBed } from '@angular/core/testing';
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';
import { vi } from 'vitest';
import { BannerValidation } from './banner-validation';

const { addListener } = vi.hoisted(() => ({
  addListener: vi.fn<(event: string, listener: (value: unknown) => void) => Promise<{ remove: () => Promise<void> }>>(),
}));

vi.mock('@capacitor-community/admob', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@capacitor-community/admob')>()),
  AdMob: {
    addListener,
    trackingAuthorizationStatus: vi.fn(),
    showBanner: vi.fn(),
    hideBanner: vi.fn(),
    resumeBanner: vi.fn(),
    removeBanner: vi.fn(),
  },
}));

describe('Banner validation failure reporting', () => {
  let component: BannerValidation;
  let emitFailure: (error: { code: number; message: string }) => void;

  beforeEach(async () => {
    vi.useFakeTimers();
    vi.resetAllMocks();
    addListener.mockImplementation(async (event, listener) => {
      if (event === BannerAdPluginEvents.FailedToLoad) emitFailure = listener;
      return { remove: async () => undefined };
    });
    vi.mocked(AdMob.trackingAuthorizationStatus).mockResolvedValue({ status: 'authorized' });
    vi.mocked(AdMob.showBanner).mockResolvedValue();
    vi.mocked(AdMob.hideBanner).mockResolvedValue();
    vi.mocked(AdMob.resumeBanner).mockResolvedValue();
    vi.mocked(AdMob.removeBanner).mockResolvedValue();
    component = TestBed.runInInjectionContext(() => new BannerValidation());
    await component.vm.enter();
  });

  afterEach(async () => {
    await component.vm.leave();
    vi.useRealTimers();
  });

  async function runValidation(): Promise<void> {
    const run = component.vm.run();
    await vi.runAllTimersAsync();
    await run;
  }

  function result(name: string): boolean | undefined {
    return component.vm.eventItems().find((item) => item.name === name)?.result;
  }

  it('passes an accepted invalid-ID request and its asynchronous failure event', async () => {
    vi.mocked(AdMob.showBanner).mockImplementation(async ({ adId }) => {
      if (adId === 'showBannerFailed') {
        setTimeout(() => emitFailure({ code: 1, message: 'Invalid ad unit ID' }), 0);
      }
    });

    await runValidation();

    expect(result('showBannerFailed')).toBe(true);
    expect(result(BannerAdPluginEvents.FailedToLoad)).toBe(true);
  });

  it('does not treat a rejected request as a successful request start', async () => {
    vi.mocked(AdMob.showBanner).mockResolvedValueOnce().mockRejectedValueOnce(new Error('Bridge unavailable'));

    await runValidation();

    expect(result('showBannerFailed')).toBe(false);
    expect(result(BannerAdPluginEvents.FailedToLoad)).toBeUndefined();
  });

  it('does not pass the failure-event check merely because the request resolves', async () => {
    await runValidation();

    expect(result('showBannerFailed')).toBe(true);
    expect(result(BannerAdPluginEvents.FailedToLoad)).toBeUndefined();
  });
});
