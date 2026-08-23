/**
 * Cross-platform style tokens supported by the plugin-owned native layouts.
 * Arbitrary HTML and platform-specific native layouts are intentionally not
 * accepted so that click tracking, AdChoices, and attribution remain under the
 * Google Mobile Ads SDK's control.
 */
export interface NativeAdStyle {
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  backgroundColor?: string;
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  borderColor?: string;
  /** Border width in logical pixels. */
  borderWidth?: number;
  /** Corner radius in logical pixels. */
  cornerRadius?: number;
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  headlineColor?: string;
  /** Scaled font size, clamped to 12–24. */
  headlineFontSize?: number;
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  bodyColor?: string;
  /** Scaled font size, clamped to 10–18. */
  bodyFontSize?: number;
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  callToActionBackgroundColor?: string;
  /** CSS-style `#RRGGBB` or `#RRGGBBAA`. */
  callToActionTextColor?: string;
  /** Scaled font size, clamped to 12–18. */
  callToActionFontSize?: number;
}
