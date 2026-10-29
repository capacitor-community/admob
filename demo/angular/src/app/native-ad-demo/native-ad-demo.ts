import { JsonPipe } from '@angular/common';
import {
  afterRenderEffect,
  ChangeDetectionStrategy,
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  signal,
  viewChild,
} from '@angular/core';
import { NativeAdFeed, NativeAdPluginEvents, NativeAdTemplate } from '@capacitor-community/admob';
import { Capacitor } from '@capacitor/core';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonToolbar,
  ViewWillEnter,
  ViewWillLeave,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  arrowUpOutline,
  bookmark,
  bookmarkOutline,
  chatbubbleOutline,
  checkmarkCircle,
  heart,
  heartOutline,
  leafOutline,
} from 'ionicons/icons';
import { ViewModelStore } from '../shared/view-model-store';

interface ArticleItem {
  kind: 'article';
  id: string;
  name: string;
  handle: string;
  initials: string;
  tone: string;
  time: string;
  text: string;
  likes: number;
  replies: number;
  image?: string;
  imageAlt?: string;
  location?: string;
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
    name: 'Emma Chen',
    handle: 'emmachen',
    initials: 'ec',
    tone: 'rose',
    time: '12m',
    text: 'A slow morning, a good coffee, and nowhere to rush. Found my new favorite corner of the city. ☕',
    likes: 128,
    replies: 12,
    image: 'assets/gather/coffee.jpg',
    imageAlt: 'Pour-over coffee being brewed by a sunlit café window',
    location: 'Little moments · Tokyo',
  },
  { kind: 'native-ad', id: 'ad-after-capacitor', slotKey: 'sponsored-after-capacitor' },
  {
    kind: 'article',
    id: 'article-ionic',
    name: 'Oliver Park',
    handle: 'oliverwalks',
    initials: 'op',
    tone: 'sage',
    time: '28m',
    text: 'Took the long way home. Turns out the best part of the day was the part I hadn’t planned. 🌿',
    likes: 86,
    replies: 8,
    location: 'Everyday discoveries',
  },
  {
    kind: 'article',
    id: 'article-weekend',
    name: 'Sofia Rivera',
    handle: 'sofiamakes',
    initials: 'sr',
    tone: 'lavender',
    time: '42m',
    text: 'Weekend idea: pick a neighborhood you’ve never visited. No itinerary. Just comfortable shoes and a little curiosity.',
    likes: 214,
    replies: 24,
  },
  { kind: 'native-ad', id: 'ad-after-ionic', slotKey: 'sponsored-after-ionic' },
  {
    kind: 'article',
    id: 'article-lifecycle',
    name: 'Emma Chen',
    handle: 'emmachen',
    initials: 'ec',
    tone: 'rose',
    time: '1h',
    text: 'What’s one little place in your city that deserves more love? Collecting recommendations for next weekend. ✨',
    likes: 53,
    replies: 19,
  },
];

@Component({
  selector: 'app-native-ad-demo',
  templateUrl: 'native-ad-demo.html',
  styleUrl: 'native-ad-demo.scss',
  imports: [JsonPipe, IonBackButton, IonButtons, IonContent, IonHeader, IonIcon, IonToolbar],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeAdDemo implements ViewWillEnter, ViewWillLeave {
  readonly content = viewChild.required(IonContent);
  readonly vm = new ViewModel(this);

  constructor() {
    addIcons({
      arrowUpOutline,
      bookmark,
      bookmarkOutline,
      chatbubbleOutline,
      checkmarkCircle,
      heart,
      heartOutline,
      leafOutline,
    });
    afterRenderEffect(() => {
      this.vm.lastEvent();
      this.vm.errorMessage();
      void this.vm.invalidateLayout();
    });
  }

  ionViewWillEnter(): void {
    void this.vm.enter();
  }

  ionViewWillLeave(): void {
    void this.vm.leave();
  }
}

class ViewModel extends ViewModelStore<NativeAdDemo> {
  readonly feedItems = feedItems;
  readonly liked = signal<ReadonlySet<string>>(new Set());
  readonly saved = signal<ReadonlySet<string>>(new Set());

  toggleLike(id: string): void {
    this.liked.update((previous) => this.#toggle(previous, id));
  }

  toggleSave(id: string): void {
    this.saved.update((previous) => this.#toggle(previous, id));
  }

  async disableHandler(event: Event, slotKey: string | null): Promise<void> {
    const button = event.currentTarget as HTMLButtonElement;
    button.disabled = true;
    const work = slotKey ? this.reload(slotKey) : this.host.content().scrollToTop(300);
    await work.finally(() => {
      button.disabled = false;
    });
  }

  #toggle(previous: ReadonlySet<string>, id: string): ReadonlySet<string> {
    const next = new Set(previous);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    return next;
  }
  readonly isNativePlatform = Capacitor.isNativePlatform();
  readonly lastEvent = signal<AdEvent | undefined>(undefined);
  readonly errorMessage = signal<string | undefined>(undefined);

  #feed?: NativeAdFeed;
  #generation = 0;
  #setup?: Promise<void>;

  async invalidateLayout(): Promise<void> {
    await this.#feed?.invalidateLayout().catch((error: unknown) => console.error(error));
  }

  async enter(): Promise<void> {
    if (!this.isNativePlatform) return;
    const generation = ++this.#generation;
    this.errorMessage.set(undefined);
    const start = () => this.#startFeed(generation);
    this.#setup = (this.#setup?.then(start) ?? start()).catch((error: unknown) => {
      if (generation === this.#generation) this.#setError(error);
    });
    await this.#setup;
  }

  async reload(slotKey: string): Promise<void> {
    this.errorMessage.set(undefined);
    await this.#feed?.reload(slotKey).catch((error: unknown) => this.#setError(error));
  }

  async leave(): Promise<void> {
    ++this.#generation;
    const feed = this.#feed;
    this.#feed = undefined;
    await feed?.destroy().catch((error: unknown) => this.#setError(error));
  }

  async #startFeed(generation: number): Promise<void> {
    if (generation !== this.#generation) return;
    const scrollElement = Capacitor.getPlatform() === 'ios' ? await this.host.content().getScrollElement() : undefined;
    if (generation !== this.#generation) return;
    const feed = await NativeAdFeed.create({
      scrollElement,
      feedId: 'native-ad-demo-feed',
      androidScrollSnapshot: Capacitor.getPlatform() === 'android',
      template: NativeAdTemplate.Medium,
      isTesting: true,
      style: {
        backgroundColor: '#ffffff',
        borderColor: '#ffffff',
        borderWidth: 0,
        cornerRadius: 0,
        headlineColor: '#24232b',
        bodyColor: '#72717c',
        callToActionTextColor: '#ffffff',
        callToActionBackgroundColor: '#cf3867',
      },
    });
    if (generation !== this.#generation) {
      await feed.destroy();
      return;
    }
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
