import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdMob, AppOpenAdPluginEvents } from '@capacitor-community/admob';
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
import { appOpenOptions } from '../../shared/ad.options';
import { ValidationResultService } from '../../shared/validation-result.service';
import { ValidationTestItem } from '../../shared/validation-test-item';
import { ViewModelStore } from '../../shared/view-model-store';

const testItems: ValidationTestItem[] = [
  { type: 'method', name: 'loadAppOpen' },
  { type: 'event', name: AppOpenAdPluginEvents.Loaded },
  { type: 'method', name: 'isAppOpenLoaded' },
  { type: 'method', name: 'showAppOpen' },
  { type: 'event', name: AppOpenAdPluginEvents.Opened },
  { type: 'method', name: 'showAppOpenFailed' },
  { type: 'event', name: AppOpenAdPluginEvents.FailedToShow, expect: 'error' },
  { type: 'event', name: AppOpenAdPluginEvents.Closed },
  { type: 'method', name: 'loadAppOpenFailed' },
  { type: 'event', name: AppOpenAdPluginEvents.FailedToLoad, expect: 'error' },
];

@Component({
  selector: 'app-app-open-validation',
  templateUrl: 'app-open-validation.html',
  styleUrl: 'app-open-validation.scss',
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppOpenValidation implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
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

class ViewModel extends ViewModelStore<AppOpenValidation> {
  readonly #resultService = inject(ValidationResultService);
  readonly #listenerHandlers: PluginListenerHandle[] = [];

  readonly eventItems = signal<ValidationTestItem[]>([]);

  async enter(): Promise<void> {
    this.eventItems.set(structuredClone(testItems));

    const handlers = await Promise.all([
      AdMob.addListener(AppOpenAdPluginEvents.Loaded, () => this.#recordEvent(AppOpenAdPluginEvents.Loaded)),
      AdMob.addListener(AppOpenAdPluginEvents.FailedToLoad, (value) =>
        this.#recordEvent(AppOpenAdPluginEvents.FailedToLoad, value),
      ),
      AdMob.addListener(AppOpenAdPluginEvents.Opened, () => {
        this.#recordEvent(AppOpenAdPluginEvents.Opened);
        void this.#record('showAppOpenFailed', AdMob.showAppOpen(), true);
      }),
      AdMob.addListener(AppOpenAdPluginEvents.Closed, () => {
        this.#recordEvent(AppOpenAdPluginEvents.Closed);
        void this.#record('loadAppOpenFailed', AdMob.loadAppOpen({ adId: 'failed' }), true);
      }),
      AdMob.addListener(AppOpenAdPluginEvents.FailedToShow, (value) =>
        this.#recordEvent(AppOpenAdPluginEvents.FailedToShow, value),
      ),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async run(): Promise<void> {
    await this.#record('loadAppOpen', AdMob.loadAppOpen(appOpenOptions));

    const isLoaded = await AdMob.isAppOpenLoaded().then(
      ({ value }) => value,
      () => false,
    );
    await this.#resultService.update(this.eventItems, 'isAppOpenLoaded', isLoaded);
    await this.#record('showAppOpen', AdMob.showAppOpen());
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

  #recordEvent(name: AppOpenAdPluginEvents, value?: unknown): void {
    void this.#resultService.update(this.eventItems, name, true, value);
  }
}
