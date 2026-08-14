import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AdMob } from '@capacitor-community/admob';
import { IonApp, IonRouterOutlet, Platform } from '@ionic/angular/standalone';
import { ViewModelStore } from './shared/view-model-store';

@Component({
  selector: 'app-root',
  templateUrl: 'app.html',
  styleUrl: 'app.scss',
  imports: [IonApp, IonRouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  readonly vm = new ViewModel(this);

  constructor() {
    void this.vm.initialize();
  }
}

class ViewModel extends ViewModelStore<App> {
  readonly #platform = inject(Platform);

  async initialize(): Promise<void> {
    await this.#platform.ready();
    await AdMob.initialize({ testingDevices: ['2077ef9a63d2b398840261c8221a0c9b'], initializeForTesting: true });
    await AdMob.setApplicationMuted({ muted: false });
    await AdMob.setApplicationVolume({ volume: 0.5 });
  }
}
