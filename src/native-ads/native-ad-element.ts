export const NATIVE_AD_ELEMENT_NAME = 'capacitor-admob-native';

interface NativeAdElementFeed {
  attach(slotKey: string, element: HTMLElement): void;
  detach(element: HTMLElement): void;
}

type NativeAdElement = HTMLElement & { connectToFeed(): void };
type NativeAdFeedLookup = (feedId: string) => NativeAdElementFeed | undefined;
type NativeAdElementRuntime = typeof globalThis & { __capacitorAdMobNativeFeedLookup?: NativeAdFeedLookup };

export const defineNativeAdElement = (findFeed: NativeAdFeedLookup): void => {
  const runtime = globalThis as NativeAdElementRuntime;
  runtime.__capacitorAdMobNativeFeedLookup = findFeed;
  if (typeof customElements === 'undefined' || customElements.get(NATIVE_AD_ELEMENT_NAME)) {
    return;
  }

  customElements.define(
    NATIVE_AD_ELEMENT_NAME,
    class extends HTMLElement {
      private attachedFeed?: NativeAdElementFeed;

      static get observedAttributes(): string[] {
        return ['feed-id', 'slot-key'];
      }

      connectedCallback(): void {
        this.connectToFeed();
      }

      disconnectedCallback(): void {
        this.disconnectFromFeed();
      }

      attributeChangedCallback(): void {
        if (!this.isConnected) {
          return;
        }
        this.disconnectFromFeed();
        this.connectToFeed();
      }

      connectToFeed(): void {
        const feedId = this.getAttribute('feed-id')?.trim();
        const slotKey = this.getAttribute('slot-key')?.trim();
        const feed = feedId ? runtime.__capacitorAdMobNativeFeedLookup?.(feedId) : undefined;
        if (!feed || !slotKey) {
          return;
        }
        try {
          feed.attach(slotKey, this);
          this.attachedFeed = feed;
        } catch (error) {
          this.dispatchEvent(new CustomEvent('nativeAdError', { bubbles: true, detail: error }));
        }
      }

      private disconnectFromFeed(): void {
        this.attachedFeed?.detach(this);
        this.attachedFeed = undefined;
      }
    },
  );
};

export const connectNativeAdElements = (): void => {
  document
    .querySelectorAll<HTMLElement>(NATIVE_AD_ELEMENT_NAME)
    .forEach((element) => (element as NativeAdElement).connectToFeed?.());
};
