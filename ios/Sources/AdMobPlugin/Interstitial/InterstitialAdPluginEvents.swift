// Preserve existing public Swift API names.
// swiftlint:disable identifier_name
public enum InterstitialAdPluginEvents: String {
    case Loaded = "interstitialAdLoaded"
    case FailedToLoad = "interstitialAdFailedToLoad"
    case Showed = "interstitialAdShowed"
    case FailedToShow = "interstitialAdFailedToShow"
    case Dismissed = "interstitialAdDismissed"
    case AdImpression = "interstitialAdImpression"
}
// swiftlint:enable identifier_name
