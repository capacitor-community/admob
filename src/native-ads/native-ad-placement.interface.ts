import type { NativeAdFeedSession, NativeAdIdentity } from './native-ad-options.interface';

export interface NativeAdRect {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface NativeAdPlacement extends NativeAdIdentity {
  /** Internal binding generation used to reject recycled DOM state. */
  generation: number;
  visible: boolean;
  rect?: NativeAdRect;
  clipRect?: NativeAdRect;
}

export interface NativeAdPlacementBatch extends NativeAdFeedSession {
  /** Monotonically increasing sequence used to reject stale bridge calls. */
  sequence: number;
  placements: NativeAdPlacement[];
}
