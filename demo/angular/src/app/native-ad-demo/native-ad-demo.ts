import { JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, CUSTOM_ELEMENTS_SCHEMA, signal } from '@angular/core';
import { NativeAdFeed, NativeAdPluginEvents, NativeAdTemplate } from '@capacitor-community/admob';
import { Capacitor } from '@capacitor/core';
import {
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonNote,
  IonTitle,
  IonToolbar,
  ViewWillEnter,
  ViewWillLeave,
} from '@ionic/angular/standalone';
import { ViewModelStore } from '../shared/view-model-store';

interface ArticleItem {
  kind: 'article';
  id: string;
  title: string;
  summary: string;
}

interface NativeAdItem {
  kind: 'native-ad';
  id: string;
  slotKey: string;
}

type FeedItem = ArticleItem | NativeAdItem;

interface AdEvent {
  name: NativeAdPluginEvents;
  value: unknown;
}

const feedItems: FeedItem[] = [
  {
    kind: 'article',
    id: 'article-capacitor',
    title: 'Build native apps with web technology',
    summary: 'Capacitor keeps application code portable while exposing native platform capabilities.',
  },
  { kind: 'native-ad', id: 'ad-after-capacitor', slotKey: 'sponsored-after-capacitor' },
  {
    kind: 'article',
    id: 'article-ionic',
    title: 'Compose a mobile UI with Ionic',
    summary: 'This page uses an ordinary Ionic scroll container with native ads inserted as feed rows.',
  },
  { kind: 'native-ad', id: 'ad-after-ionic', slotKey: 'sponsored-after-ionic' },
  {
    kind: 'article',
    id: 'article-lifecycle',
    title: 'Keep feed lifecycle explicit',
    summary: 'The demo creates one feed on entry and destroys its listeners and native resources on exit.',
  },
];

@Component({
  selector: 'app-native-ad-demo',
  templateUrl: 'native-ad-demo.html',
  styleUrl: 'native-ad-demo.scss',
  imports: [
    JsonPipe,
    IonCard,
    IonCardContent,
    IonCardHeader,
    IonCardTitle,
    IonContent,
    IonHeader,
    IonItem,
    IonLabel,
    IonList,
    IonListHeader,
    IonNote,
    IonTitle,
    IonToolbar,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeAdDemo implements ViewWillEnter, ViewWillLeave {
  readonly vm = new ViewModel(this);
  #setup?: Promise<void>;

  ionViewWillEnter(): void {
    this.#setup = this.vm.enter();
  }

  ionViewWillLeave(): void {
    void this.#setup?.then(() => this.vm.leave());
  }
}

class ViewModel extends ViewModelStore<NativeAdDemo> {
  readonly feedItems = feedItems;
  readonly isNativePlatform = Capacitor.isNativePlatform();
  readonly lastEvent = signal<AdEvent | undefined>(undefined);
  readonly errorMessage = signal<string | undefined>(undefined);

  #feed?: NativeAdFeed;

  async enter(): Promise<void> {
    if (!this.isNativePlatform) return;
    this.errorMessage.set(undefined);
    await this.#startFeed().catch((error: unknown) => this.#setError(error));
  }

  async reload(slotKey: string): Promise<void> {
    this.errorMessage.set(undefined);
    await this.#feed?.reload(slotKey).catch((error: unknown) => this.#setError(error));
  }

  async leave(): Promise<void> {
    const feed = this.#feed;
    this.#feed = undefined;
    await feed?.destroy().catch((error: unknown) => this.#setError(error));
  }

  async #startFeed(): Promise<void> {
    const feed = await NativeAdFeed.create({
      feedId: 'native-ad-demo-feed',
      template: NativeAdTemplate.Medium,
      isTesting: true,
      style: {
        backgroundColor: '#ffffff',
        borderColor: '#d7d8da',
        borderWidth: 1,
        cornerRadius: 12,
        headlineColor: '#1f2937',
        callToActionBackgroundColor: '#3880ff',
      },
    });
    this.#feed = feed;

    await Promise.all([
      this.#listen(feed, NativeAdPluginEvents.Loaded),
      feed.addListener(NativeAdPluginEvents.FailedToLoad, (value) =>
        this.lastEvent.set({ name: NativeAdPluginEvents.FailedToLoad, value }),
      ),
      this.#listen(feed, NativeAdPluginEvents.Clicked),
      this.#listen(feed, NativeAdPluginEvents.AdImpression),
      this.#listen(feed, NativeAdPluginEvents.Opened),
      this.#listen(feed, NativeAdPluginEvents.Closed),
      feed.addListener(NativeAdPluginEvents.AdPaid, (value) =>
        this.lastEvent.set({ name: NativeAdPluginEvents.AdPaid, value }),
      ),
    ]);
  }

  #listen(
    feed: NativeAdFeed,
    eventName:
      | NativeAdPluginEvents.Loaded
      | NativeAdPluginEvents.Clicked
      | NativeAdPluginEvents.AdImpression
      | NativeAdPluginEvents.Opened
      | NativeAdPluginEvents.Closed,
  ) {
    return feed.addListener(eventName, (value) => this.lastEvent.set({ name: eventName, value }));
  }

  #setError(error: unknown): void {
    this.errorMessage.set(error instanceof Error ? error.message : String(error));
  }
}
