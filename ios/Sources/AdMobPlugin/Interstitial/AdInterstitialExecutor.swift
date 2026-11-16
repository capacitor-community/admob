import Foundation
import Capacitor
import GoogleMobileAds

class AdInterstitialExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    private var webViewLayout: FullScreenAdWebViewLayout?
    private var preparedAds: [String: InterstitialAd] = [:]
    private var preparedAdExpirations: [String: AdExpiration] = [:]
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
                        "code": (error as NSError).code,
                        "message": error.localizedDescription
                    ])
                    call.reject(error.localizedDescription, String((error as NSError).code), error)
                    return
                }

                if let ad = ad {
                    ad.fullScreenContentDelegate = self
                    ad.paidEventHandler = { adValue in
                        let networkName = ad.responseInfo.loadedAdNetworkResponseInfo?.adNetworkClassName ?? ""
                        let impressionId = ad.responseInfo.responseIdentifier ?? ""
                        self.plugin?.notifyListeners(InterstitialAdPluginEvents.AdImpression.rawValue, data: [
                            "adUnitId": adUnitID,
                            "valueMicros": adValue.value.multiplying(byPowerOf10: 6).int64Value,
                            "currencyCode": adValue.currencyCode,
                            "precision": adValue.precision.rawValue,
                            "networkName": networkName,
                            "impressionId": impressionId
                        ])
                    }
                    self.preparedAds[adUnitID] = ad
                    self.preparedAdExpirations[adUnitID] = AdExpiration(maxAge: 3600)
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

        if preparedAdExpirations[adId]?.isExpired == true {
            preparedAds.removeValue(forKey: adId)
            preparedAdExpirations.removeValue(forKey: adId)
            if lastPreparedAdId == adId { lastPreparedAdId = Array(preparedAds.keys).last }
            let message = "Interstitial ad expired. Prepare a new ad before showing it."
            call.reject(message, "AD_EXPIRED")
            plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToShow.rawValue,
                                    data: ["code": -1, "message": message])
            return
        }

        if let rootViewController = plugin?.getRootVC() {
            if let ad = preparedAds[adId] {
                currentlyShowingAdId = adId
                webViewLayout = FullScreenAdWebViewLayout(webView: plugin?.bridge?.webView)
                ad.present(from: rootViewController)
                call.resolve([:])
            } else {
                NSLog("Ad wasn't ready")
                call.reject("Ad wasn't ready")
            }
        }
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        self.plugin?.resumeWebMedia(for: ad)
        removeCurrentlyShowingAd()
        NSLog("InterstitialFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        self.plugin?.suspendWebMedia(for: ad)
        NSLog("InterstitialFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        self.plugin?.resumeWebMedia(for: ad)
        removeCurrentlyShowingAd()
        NSLog("InterstitialFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }

    private func removeCurrentlyShowingAd() {
        guard let adId = currentlyShowingAdId else { return }
        let layout = webViewLayout
        webViewLayout = nil
        DispatchQueue.main.async { layout?.restore() }
        preparedAds.removeValue(forKey: adId)
        preparedAdExpirations.removeValue(forKey: adId)
        if lastPreparedAdId == adId { lastPreparedAdId = Array(preparedAds.keys).last }
        currentlyShowingAdId = nil
    }
}
