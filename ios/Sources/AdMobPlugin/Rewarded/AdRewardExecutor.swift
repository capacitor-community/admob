import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    var rewardedAd: RewardedAd!

    func prepareRewardVideoAd(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        RewardedAd.load(
            with: adUnitID,
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

                if let providedOptions = call.getObject("ssv") {
                    let ssvOptions = ServerSideVerificationOptions()

                    if let customData = providedOptions["customData"] as? String {
                        NSLog("Sending Custom Data: \(customData) to SSV callback")
                        ssvOptions.customRewardText = customData
                    }

                    if let userId = providedOptions["userId"] as? String {
                        NSLog("Sending UserId: \(userId) to SSV callback")
                        ssvOptions.userIdentifier = userId
                    }

                    self.rewardedAd?.serverSideVerificationOptions = ssvOptions
                }

                self.rewardedAd?.fullScreenContentDelegate = self
                self.plugin?.notifyListeners(RewardAdPluginEvents.Loaded.rawValue, data: [
                    "adUnitId": adUnitID
                ])
                call.resolve([
                    "adUnitId": adUnitID
                ])
            }
        )
    }

    func showRewardVideoAd(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            if let ad = self.rewardedAd {
                ad.present(from: rootViewController,
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

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("RewardFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(RewardAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(RewardAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardAdPluginEvents.Dismissed.rawValue, data: [:])
    }
}
