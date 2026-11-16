import Foundation
import Capacitor
import GoogleMobileAds

class AdRewardInterstitialExecutor: NSObject, FullScreenContentDelegate {
    weak var plugin: AdMobPlugin?
    private var webViewLayout: FullScreenAdWebViewLayout?
    private var preparedAds: [String: RewardedInterstitialAd] = [:]
    private var lastPreparedAdId: String?
    private var currentShow: RewardedAdShowCall?

    func prepareRewardInterstitialAd(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        RewardedInterstitialAd.load(
            with: adUnitID,
            request: request,
            completionHandler: { (ad, error) in
                if let error = error {
                    NSLog("Rewarded interstitial ad failed to load with error: \(error.localizedDescription)")
                    self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToLoad.rawValue, data: [
                        "code": (error as NSError).code,
                        "message": error.localizedDescription
                    ])
                    call.reject(error.localizedDescription, String((error as NSError).code), error)
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
                        "valueMicros": adValue.value.multiplying(byPowerOf10: 6).int64Value,
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
        guard currentShow == nil else {
            call.reject("An ad is already showing", "REWARDED_AD_ALREADY_SHOWING")
            return
        }

        guard let rootViewController = plugin?.getRootVC() else {
            call.reject("No root view controller available", "REWARDED_AD_SHOW_FAILED")
            return
        }
        guard let ad = preparedAds[adId] else {
            call.reject("Reward Video is Not Ready Yet")
            return
        }
        let show = RewardedAdShowCall(ad: ad, adId: adId, call: call)
        currentShow = show
        webViewLayout = FullScreenAdWebViewLayout(webView: plugin?.bridge?.webView)
        ad.present(from: rootViewController, userDidEarnRewardHandler: { [weak self, weak ad, weak show] in
            guard let ad = ad, let show = show else { return }
            let reward = ad.adReward
            let item: [String: Any] = ["type": reward.type, "amount": reward.amount]
            if show.reward(item) {
                self?.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Rewarded.rawValue, data: item)
            }
        })
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        self.plugin?.resumeWebMedia(for: ad)
        guard finishShow(ad, error: error as NSError) else { return }
        NSLog("RewardFullScreenDelegate Ad failed to present full screen content with error \(error.localizedDescription).")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.FailedToShow.rawValue, data: [
            "code": (error as NSError).code,
            "message": error.localizedDescription
        ])
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        self.plugin?.suspendWebMedia(for: ad)
        NSLog("RewardFullScreenDelegate Ad did present full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Showed.rawValue, data: [:])
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        self.plugin?.resumeWebMedia(for: ad)
        guard finishShow(ad) else { return }
        NSLog("RewardFullScreenDelegate Ad did dismiss full screen content.")
        self.plugin?.notifyListeners(RewardInterstitialAdPluginEvents.Dismissed.rawValue, data: [:])
    }

    private func finishShow(_ ad: FullScreenPresentingAd, error: NSError? = nil) -> Bool {
        guard let show = currentShow, show.ad === (ad as AnyObject) else { return false }
        let layout = webViewLayout
        webViewLayout = nil
        DispatchQueue.main.async { layout?.restore() }
        if preparedAds[show.adId] === show.ad {
            preparedAds.removeValue(forKey: show.adId)
        }
        if lastPreparedAdId == show.adId { lastPreparedAdId = Array(preparedAds.keys).last }
        currentShow = nil
        return show.finish(error: error)
    }
}
