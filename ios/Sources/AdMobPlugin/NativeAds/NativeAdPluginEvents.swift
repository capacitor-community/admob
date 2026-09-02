import Foundation

enum NativeAdPluginEvents: String {
    case loaded = "nativeAdLoaded"
    case failedToLoad = "nativeAdFailedToLoad"
    case clicked = "nativeAdClicked"
    case impression = "nativeAdImpression"
    case opened = "nativeAdOpened"
    case closed = "nativeAdClosed"
    case adPaid = "nativeAdPaid"
}
