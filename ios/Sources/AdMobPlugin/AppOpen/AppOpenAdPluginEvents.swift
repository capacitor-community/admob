// Preserve existing public Swift API names.
// swiftlint:disable identifier_name
public enum AppOpenAdPluginEvents: String {
    case Loaded = "appOpenAdLoaded"
    case FailedToLoad = "appOpenAdFailedToLoad"
    case Opened = "appOpenAdOpened"
    case Closed = "appOpenAdClosed"
    case FailedToShow = "appOpenAdFailedToShow"
    case AdImpression = "appOpenAdImpression"
}
// swiftlint:enable identifier_name
