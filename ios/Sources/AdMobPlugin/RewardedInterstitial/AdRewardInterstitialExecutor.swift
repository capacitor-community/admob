import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardInterstitialExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    private var preparedAds: [String: RewardedInterstitialAd] = [:]
    private var lastPreparedAdId: String?
    private var currentlyShowingAdId: String?

    func prepareRewardInterstitialAd(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        RewardedInterstitialAd.load(
            with: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Rewarded interstitial ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToLoad.rawValue, data: [
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
                    self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.AdImpression.rawValue, data: [
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
                            self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Rewarded.rawValue, data: ["type": reward.type, "amount": reward.amount])
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
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        NSLog("RewardFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        removeCurrentlyShowingAd()
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }

    private func removeCurrentlyShowingAd() {
        guard let adId = currentlyShowingAdId else { return }
        preparedAds.removeValue(forKey: adId)
        if lastPreparedAdId == adId { lastPreparedAdId = Array(preparedAds.keys).last }
        currentlyShowingAdId = nil
    }
}
