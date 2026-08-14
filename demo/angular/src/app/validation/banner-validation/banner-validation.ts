import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdMob, BannerAdPluginEvents } from '@capacitor-community/admob';
import { PluginListenerHandle } from '@capacitor/core';
import {
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonTitle,
  IonToolbar,
  ViewDidEnter,
  ViewWillEnter,
  ViewWillLeave,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { checkmarkCircle, notificationsCircleOutline, playOutline } from 'ionicons/icons';
import { bannerBottomOptions } from '../../shared/ad.options';
import { ValidationResultService } from '../../shared/validation-result.service';
import { ValidationTestItem } from '../../shared/validation-test-item';
import { ViewModelStore } from '../../shared/view-model-store';

const testItems: ValidationTestItem[] = [
  {
    type: 'method',
    name: 'trackingAuthorizationStatus',
    expect: ['authorized', 'denied', 'notDetermined', 'restricted'],
  },
  { type: 'method', name: 'showBanner' },
  { type: 'event', name: BannerAdPluginEvents.SizeChanged, expect: 1 },
  { type: 'event', name: BannerAdPluginEvents.Loaded },
  { type: 'method', name: 'hideBanner' },
  { type: 'event', name: BannerAdPluginEvents.SizeChanged, expect: 0 },
  { type: 'method', name: 'resumeBanner' },
  { type: 'event', name: BannerAdPluginEvents.SizeChanged, expect: 1 },
  { type: 'method', name: 'removeBanner' },
  { type: 'event', name: BannerAdPluginEvents.SizeChanged, expect: 0 },
  { type: 'method', name: 'showBannerFailed' },
  { type: 'event', name: BannerAdPluginEvents.FailedToLoad, expect: 'error' },
];

@Component({
  selector: 'app-banner-validation',
  templateUrl: 'banner-validation.html',
  styleUrl: 'banner-validation.scss',
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BannerValidation implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
  readonly vm = new ViewModel(this);
  #listenerSetup?: Promise<void>;

  constructor() {
    addIcons({ playOutline, notificationsCircleOutline, checkmarkCircle });
  }

  ionViewWillEnter(): void {
    this.#listenerSetup = this.vm.enter();
  }

  async ionViewDidEnter(): Promise<void> {
    await this.#listenerSetup;
    await this.vm.run();
  }

  ionViewWillLeave(): void {
    void this.#listenerSetup?.then(() => this.vm.leave());
  }
}

class ViewModel extends ViewModelStore<BannerValidation> {
  readonly #resultService = inject(ValidationResultService);
  readonly #listenerHandlers: PluginListenerHandle[] = [];

  readonly eventItems = signal<ValidationTestItem[]>([]);

  async enter(): Promise<void> {
    this.eventItems.set(structuredClone(testItems));
    const handlers = await Promise.all([
      AdMob.addListener(BannerAdPluginEvents.SizeChanged, (value) =>
        this.#recordEvent(BannerAdPluginEvents.SizeChanged, value),
      ),
      AdMob.addListener(BannerAdPluginEvents.Loaded, () => this.#recordEvent(BannerAdPluginEvents.Loaded)),
      AdMob.addListener(BannerAdPluginEvents.FailedToLoad, (value) =>
        this.#recordEvent(BannerAdPluginEvents.FailedToLoad, value),
      ),
      AdMob.addListener(BannerAdPluginEvents.Opened, () => this.#recordEvent(BannerAdPluginEvents.Opened)),
      AdMob.addListener(BannerAdPluginEvents.Closed, () => this.#recordEvent(BannerAdPluginEvents.Closed)),
      AdMob.addListener(BannerAdPluginEvents.AdImpression, () => this.#recordEvent(BannerAdPluginEvents.AdImpression)),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async run(): Promise<void> {
    const trackingStatus = await AdMob.trackingAuthorizationStatus().catch(() => undefined);
    await this.#resultService.update(
      this.eventItems,
      'trackingAuthorizationStatus',
      trackingStatus === undefined ? false : undefined,
      trackingStatus?.status,
    );

    await this.#record('showBanner', AdMob.showBanner(bannerBottomOptions));
    await this.#record('hideBanner', AdMob.hideBanner());
    await this.#record('resumeBanner', AdMob.resumeBanner());
    await this.#record('removeBanner', AdMob.removeBanner());
    await this.#record('showBannerFailed', AdMob.showBanner({ adId: 'showBannerFailed' }), true);
  }

  async leave(): Promise<void> {
    await Promise.all(this.#listenerHandlers.splice(0).map((handler) => handler.remove()));
  }

  async #record(name: string, operation: Promise<unknown>, expectFailure = false): Promise<void> {
    const succeeded = await operation.then(
      () => true,
      () => false,
    );
    await this.#resultService.update(this.eventItems, name, expectFailure ? !succeeded : succeeded);
  }

  #recordEvent(name: BannerAdPluginEvents, value?: unknown): void {
    void this.#resultService.update(this.eventItems, name, true, value);
  }
}
