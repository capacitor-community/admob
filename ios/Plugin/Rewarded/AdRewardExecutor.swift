import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardExecutor: NSObject, GADFullScreenContentDelegate {
    public weak var plugin: CAPPlugin?
    var rewardedAd: GADRewardedAd!

    func prepareRewardVideoAd(_ call: CAPPluginCall, _ request: GADRequest, _ adUnitID: String) {
        GADRewardedAd.load(
            withAdUnitID: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Rewarded ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(RewardAdPluginEvents.FailedToLoad.rawValue, data: [
                        "code": 0,
                        "message": error.localizedDescription
                    ])
                    call.reject("Loading failed")
                    return
                }

                self.rewardedAd = ad
                self.rewardedAd?.fullScreenContentDelegate = self
                self.plugin?.notifyListeners(RewardAdPluginEvents.Loaded.rawValue, data: [
                    "adUnitId": adUnitID
                ])
                call.resolve([:])
            }
        )
    }

    func showRewardVideoAd(_ call: CAPPluginCall) {
        if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
            if let ad = self.rewardedAd {
                ad.present(fromRootViewController: rootViewController,
                           userDidEarnRewardHandler: {
                            let reward = ad.adReward
                            self.plugin?.notifyListeners(RewardAdPluginEvents.Rewarded.rawValue, data: ["type": reward.type, "amount": reward.amount])
                            call.resolve(["type": reward.type, "amount": reward.amount])
                           }
                )
            } else {
                call.reject("Reward Video is Not Ready Yet")
            }
        }
    }

    public func adDidPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(RewardAdPluginEvents.Showed.rawValue, data: [:])
    }

    public func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("RewardFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(RewardAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    public func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardAdPluginEvents.Dismissed.rawValue, data: [:])
    }
}
