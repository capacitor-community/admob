import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdMob, InterstitialAdPluginEvents } from '@capacitor-community/admob';
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
import { interstitialOptions } from '../../shared/ad.options';
import { ValidationResultService } from '../../shared/validation-result.service';
import { ValidationTestItem } from '../../shared/validation-test-item';
import { ViewModelStore } from '../../shared/view-model-store';

const testItems: ValidationTestItem[] = [
  { type: 'method', name: 'prepareInterstitial' },
  { type: 'method', name: 'showInterstitial' },
  { type: 'event', name: InterstitialAdPluginEvents.Loaded },
  { type: 'event', name: InterstitialAdPluginEvents.Showed },
  { type: 'event', name: InterstitialAdPluginEvents.Dismissed },
  { type: 'method', name: 'prepareInterstitialFailed' },
  { type: 'event', name: InterstitialAdPluginEvents.FailedToLoad, expect: 'error' },
];

@Component({
  selector: 'app-interstitial-validation',
  templateUrl: 'interstitial-validation.html',
  styleUrl: 'interstitial-validation.scss',
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InterstitialValidation implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
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

class ViewModel extends ViewModelStore<InterstitialValidation> {
  readonly #resultService = inject(ValidationResultService);
  readonly #listenerHandlers: PluginListenerHandle[] = [];

  readonly eventItems = signal<ValidationTestItem[]>([]);

  async enter(): Promise<void> {
    this.eventItems.set(structuredClone(testItems));

    const handlers = await Promise.all([
      AdMob.addListener(InterstitialAdPluginEvents.FailedToLoad, (value) =>
        this.#recordEvent(InterstitialAdPluginEvents.FailedToLoad, value),
      ),
      AdMob.addListener(InterstitialAdPluginEvents.Loaded, (value) =>
        this.#recordEvent(InterstitialAdPluginEvents.Loaded, value),
      ),
      AdMob.addListener(InterstitialAdPluginEvents.Dismissed, () => {
        this.#recordEvent(InterstitialAdPluginEvents.Dismissed);
        void this.#record('prepareInterstitialFailed', AdMob.prepareInterstitial({ adId: 'failed' }), true);
      }),
      AdMob.addListener(InterstitialAdPluginEvents.FailedToShow, (value) =>
        this.#recordEvent(InterstitialAdPluginEvents.FailedToShow, value),
      ),
      AdMob.addListener(InterstitialAdPluginEvents.Showed, () => this.#recordEvent(InterstitialAdPluginEvents.Showed)),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async run(): Promise<void> {
    const prepared = await AdMob.prepareInterstitial(interstitialOptions).then(
      ({ adUnitId }) => Boolean(adUnitId),
      () => false,
    );
    await this.#resultService.update(this.eventItems, 'prepareInterstitial', prepared);
    await this.#record('showInterstitial', AdMob.showInterstitial());
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

  #recordEvent(name: InterstitialAdPluginEvents, value?: unknown): void {
    void this.#resultService.update(this.eventItems, name, true, value);
  }
}
