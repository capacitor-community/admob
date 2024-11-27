import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardInterstitialExecutor: NSObject, GADFullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    var rewardedInterstitialAd: GADRewardedInterstitialAd!

    func prepareRewardInterstitialAd(_ call: CAPPluginCall, _ request: GADRequest, _ adUnitID: String) {
        GADRewardedInterstitialAd.load(
            withAdUnitID: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Rewarded ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToLoad.rawValue, data: [
                        "code": 0,
                        "message": error.localizedDescription
                    ])
                    call.reject("Loading failed")
                    return
                }

                self.rewardedInterstitialAd = ad

                if let providedOptions = call.getObject("ssv") {
                    let ssvOptions = GADServerSideVerificationOptions()

                    if let customData = providedOptions["customData"] as? String {
                        NSLog("Sending Custom Data: \(customData) to SSV callback")
                        ssvOptions.customRewardString = customData
                    }

                    if let userId = providedOptions["userId"] as? String {
                        NSLog("Sending UserId: \(userId) to SSV callback")
                        ssvOptions.userIdentifier = userId
                    }

                    self.rewardedInterstitialAd?.serverSideVerificationOptions = ssvOptions
                }

                self.rewardedInterstitialAd?.fullScreenContentDelegate = self
                self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Loaded.rawValue, data: [
                    "adUnitId": adUnitID
                ])
                call.resolve([
                    "adUnitId": adUnitID
                ])
            }
        )
    }

    func showRewardInterstitialAd(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            if let ad = self.rewardedInterstitialAd {
                ad.present(fromRootViewController: rootViewController,
                           userDidEarnRewardHandler: {
                            let reward = ad.adReward
                            self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Rewarded.rawValue, data: ["type": reward.type, "amount": reward.amount])
                            call.resolve(["type": reward.type, "amount": reward.amount])
                           }
                )
            } else {
                call.reject("Reward Video is Not Ready Yet")
            }
        }
    }

    func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("RewardFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }
}
