import Foundation
import Capacitor
import GoogleMobileAds

class AdInterstitialExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    var interstitial: InterstitialAd!

    func prepareInterstitial(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        InterstitialAd.load(
            with: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Rewarded ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToLoad.rawValue, data: [
                        "code": 0,
                        "message": error.localizedDescription
                    ])
                    call.reject("Loading failed")
                    return
                }

                self.interstitial = ad
                self.interstitial.fullScreenContentDelegate = self
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
        if let rootViewController = plugin?.getRootVC() {
            if let ad = self.interstitial {
                ad.present(from: rootViewController)
                call.resolve([:])
            } else {
                NSLog("Ad wasn't ready")
                call.reject("Ad wasn't ready")
            }
        }
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
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
        NSLog("InterstitialFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }
}
