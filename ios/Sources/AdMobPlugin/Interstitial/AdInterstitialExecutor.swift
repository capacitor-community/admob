import Foundation
import Capacitor
import GoogleMobileAds

class AdInterstitialExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    private var preparedAds: [String: InterstitialAd] = [:]
    private var lastPreparedAdId: String?
    private var currentlyShowingAdId: String?

    func prepareInterstitial(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        InterstitialAd.load(
            with: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Interstitial ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToLoad.rawValue, data: [
                        "code": 0,
                        "message": error.localizedDescription
                    ])
                    call.reject("Loading failed")
                    return
                }

                if let ad = ad {
                    ad.fullScreenContentDelegate = self
                    ad.paidEventHandler = { adValue in
                        let networkName = ad.responseInfo.loadedAdNetworkResponseInfo?.adNetworkClassName ?? ""
                        let impressionId = ad.responseInfo.responseIdentifier ?? ""
                        self.plugin?.notifyListeners(InterstitialAdPluginEvents.AdImpression.rawValue, data: [
                            "adUnitId": adUnitID,
                            "valueMicros": adValue.value.int64Value,
                            "currencyCode": adValue.currencyCode,
                            "precision": adValue.precision.rawValue,
                            "networkName": networkName,
                            "impressionId": impressionId
                        ])
                    }
                    self.preparedAds[adUnitID] = ad
                    self.lastPreparedAdId = adUnitID
                }
                self.plugin?.notifyListeners(InterstitialAdPluginEvents.Loaded.rawValue, data: [
                    "adUnitId": adUnitID
                ])
                call.resolve([
                    "adUnitId": adUnitID
                ])
            }
        )
    }

    func showInterstitial(_ call: CAPPluginCall) {
        guard let adId = call.getString("adId") ?? lastPreparedAdId else {
            call.reject("No ad prepared")
            return
        }
        guard currentlyShowingAdId == nil else {
            call.reject("An ad is already showing")
            return
        }

        if let rootViewController = plugin?.getRootVC() {
            if let ad = preparedAds[adId] {
                currentlyShowingAdId = adId
                ad.present(from: rootViewController)
                call.resolve([:])
            } else {
                NSLog("Ad wasn't ready")
                call.reject("Ad wasn't ready")
            }
        }
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        removeCurrentlyShowingAd()
        NSLog("InterstitialFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        NSLog("InterstitialFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        removeCurrentlyShowingAd()
        NSLog("InterstitialFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }

    private func removeCurrentlyShowingAd() {
        guard let adId = currentlyShowingAdId else { return }
        preparedAds.removeValue(forKey: adId)
        if lastPreparedAdId == adId { lastPreparedAdId = Array(preparedAds.keys).last }
        currentlyShowingAdId = nil
    }
}
