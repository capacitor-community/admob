import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdMob, RewardAdPluginEvents } from '@capacitor-community/admob';
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
import { rewardOptions } from '../../shared/ad.options';
import { ValidationResultService } from '../../shared/validation-result.service';
import { ValidationTestItem } from '../../shared/validation-test-item';
import { ViewModelStore } from '../../shared/view-model-store';

const testItems: ValidationTestItem[] = [
  { type: 'method', name: 'prepareRewardVideoAd' },
  { type: 'method', name: 'showRewardVideoAd' },
  { type: 'event', name: RewardAdPluginEvents.Loaded },
  { type: 'event', name: RewardAdPluginEvents.Showed },
  { type: 'event', name: RewardAdPluginEvents.Rewarded },
  { type: 'event', name: RewardAdPluginEvents.Dismissed },
  { type: 'method', name: 'prepareRewardVideoAdFailed' },
  { type: 'event', name: RewardAdPluginEvents.FailedToLoad, expect: 'error' },
];

@Component({
  selector: 'app-reward-validation',
  templateUrl: 'reward-validation.html',
  styleUrl: 'reward-validation.scss',
  imports: [IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonListHeader, IonLabel, IonItem, IonIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RewardValidation implements ViewDidEnter, ViewWillEnter, ViewWillLeave {
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

class ViewModel extends ViewModelStore<RewardValidation> {
  readonly #resultService = inject(ValidationResultService);
  readonly #listenerHandlers: PluginListenerHandle[] = [];

  readonly eventItems = signal<ValidationTestItem[]>([]);

  async enter(): Promise<void> {
    this.eventItems.set(structuredClone(testItems));

    const handlers = await Promise.all([
      AdMob.addListener(RewardAdPluginEvents.FailedToLoad, (value) =>
        this.#recordEvent(RewardAdPluginEvents.FailedToLoad, value),
      ),
      AdMob.addListener(RewardAdPluginEvents.Loaded, (value) => this.#recordEvent(RewardAdPluginEvents.Loaded, value)),
      AdMob.addListener(RewardAdPluginEvents.Rewarded, (value) =>
        this.#recordEvent(RewardAdPluginEvents.Rewarded, value),
      ),
      AdMob.addListener(RewardAdPluginEvents.Dismissed, () => {
        this.#recordEvent(RewardAdPluginEvents.Dismissed);
        void this.#record('prepareRewardVideoAdFailed', AdMob.prepareRewardVideoAd({ adId: 'failed' }), true);
      }),
      AdMob.addListener(RewardAdPluginEvents.FailedToShow, (value) =>
        this.#recordEvent(RewardAdPluginEvents.FailedToShow, value),
      ),
      AdMob.addListener(RewardAdPluginEvents.Showed, () => this.#recordEvent(RewardAdPluginEvents.Showed)),
    ]);
    this.#listenerHandlers.push(...handlers);
  }

  async run(): Promise<void> {
    const prepared = await AdMob.prepareRewardVideoAd(rewardOptions).then(
      ({ adUnitId }) => Boolean(adUnitId),
      () => false,
    );
    await this.#resultService.update(this.eventItems, 'prepareRewardVideoAd', prepared);
    await this.#record('showRewardVideoAd', AdMob.showRewardVideoAd());
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

  #recordEvent(name: RewardAdPluginEvents, value?: unknown): void {
    void this.#resultService.update(this.eventItems, name, true, value);
  }
}
