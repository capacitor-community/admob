import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    private var preparedAds: [String: RewardedAd] = [:]
    private var lastPreparedAdId: String?
    private var currentlyShowingAdId: String?

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

                guard let ad = ad else {
                    call.reject("Loading failed")
                    return
                }

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

                    ad.serverSideVerificationOptions = ssvOptions
                }

                ad.fullScreenContentDelegate = self
                ad.paidEventHandler = { adValue in
                    let networkName = ad.responseInfo.loadedAdNetworkResponseInfo?.adNetworkClassName ?? ""
                    let impressionId = ad.responseInfo.responseIdentifier ?? ""
                    self.plugin?.notifyListeners(RewardAdPluginEvents.AdImpression.rawValue, data: [
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
        removeCurrentlyShowingAd()
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
        removeCurrentlyShowingAd()
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardAdPluginEvents.Dismissed.rawValue, data: [:])
    }

    private func removeCurrentlyShowingAd() {
        guard let adId = currentlyShowingAdId else { return }
        preparedAds.removeValue(forKey: adId)
        if lastPreparedAdId == adId { lastPreparedAdId = Array(preparedAds.keys).last }
        currentlyShowingAdId = nil
    }
}
