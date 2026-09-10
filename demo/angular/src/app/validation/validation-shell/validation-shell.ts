import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonIcon, IonLabel, IonTabBar, IonTabButton, IonTabs } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { easel, newspaper, phonePortrait, play, reader, videocam } from 'ionicons/icons';
import { ViewModelStore } from '../../shared/view-model-store';

@Component({
  selector: 'app-validation-shell',
  templateUrl: 'validation-shell.html',
  styleUrl: 'validation-shell.scss',
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ValidationShell {
  readonly vm = new ViewModel(this);

  constructor() {
    addIcons({ play, easel, newspaper, reader, videocam, phonePortrait });
  }
}

class ViewModel extends ViewModelStore<ValidationShell> {}
