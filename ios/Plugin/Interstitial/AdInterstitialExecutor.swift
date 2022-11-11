import Foundation
import Capacitor
import GoogleMobileAds

class AdInterstitialExecutor: NSObject, GADFullScreenContentDelegate {
    public weak var plugin: CAPPlugin?
    var interstitial: GADInterstitialAd!

    func prepareInterstitial(_ call: CAPPluginCall, _ request: GADRequest, _ adUnitID: String) {
        GADInterstitialAd.load(
            withAdUnitID: adUnitID,
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
                call.resolve([:])
            }
        )
    }

    func showInterstitial(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.bridge?.viewController {
            if let ad = self.interstitial {
                ad.present(fromRootViewController: rootViewController)
                call.resolve([:])
            } else {
                NSLog("Ad wasn't ready")
                call.reject("Ad wasn't ready")
            }
        }
    }

    public func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("InterstitialFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    public func adWillPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("InterstitialFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    public func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("InterstitialFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(InterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }
}
