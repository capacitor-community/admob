// Preserve existing public Swift API names.
// swiftlint:disable identifier_name
public enum BannerAdPluginEvents: String {
    case SizeChanged = "bannerAdSizeChanged"
    case Closed = "bannerAdClosed"
    case FailedToLoad = "bannerAdFailedToLoad"
    case Opened = "bannerAdOpened"
    case Loaded = "bannerAdLoaded"
    case AdImpression = "bannerAdImpression"
    case AdPaid = "bannerAdPaid"
}
// swiftlint:enable identifier_name
