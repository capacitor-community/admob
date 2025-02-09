public enum FullScreenAdEventName: String {
    case onAdLoaded = "onAdLoaded"
    case onAdFailedToLoad = "onAdFailedToLoad"
    case adDidPresentFullScreenContent = "adDidPresentFullScreenContent"
    case didFailToPresentFullScreenContentWithError = "didFailToPresentFullScreenContentWithError"

    /**
     * Follow iOS Event Name
     * https://developers.google.com/admob/ios/api/reference/Protocols/GADFullScreenContentDelegate#-addidpresentfullscreencontent:
     */
    case adDidDismissFullScreenContent = "adDidDismissFullScreenContent"
}
