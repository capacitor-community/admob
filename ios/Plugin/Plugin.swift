import Foundation
import Capacitor
import GoogleMobileAds
#if canImport(AppTrackingTransparency)
import AppTrackingTransparency
#endif

@objc(AdMob)
public class AdMob: CAPPlugin {

    var testingDevices: [String] = []

    private let bannerExecutor = BannerExecutor()
    private let adInterstitialExecutor = AdInterstitialExecutor()
    private let adRewardExecutor = AdRewardExecutor()

    /**
     * Enable SKAdNetwork to track conversions
     * https://developers.google.com/admob/ios/ios14
     */
    @objc func initialize(_ call: CAPPluginCall) {
        self.bannerExecutor.plugin = self
        self.adInterstitialExecutor.plugin = self
        self.adRewardExecutor.plugin = self

        let isTrack = call.getBool("requestTrackingAuthorization") ?? true

        if call.getBool("initializeForTesting") ?? false {
            GADMobileAds.sharedInstance().requestConfiguration.testDeviceIdentifiers = call.getArray("testingDevices", String.self) ?? []
        }

        if !isTrack {
            GADMobileAds.sharedInstance().start(completionHandler: nil)
        } else if #available(iOS 14, *) {
            #if canImport(AppTrackingTransparency)
            ATTrackingManager.requestTrackingAuthorization(completionHandler: { _ in
                // iOS >= 14
                GADMobileAds.sharedInstance().start(completionHandler: nil)
            })
            #else
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            #endif
        } else {
            // iOS < 14
            GADMobileAds.sharedInstance().start(completionHandler: nil)
        }

        call.resolve([:])
    }

    @objc func globalSettings(_ call: CAPPluginCall) {

        if call.getFloat("volume") != nil {
            GADMobileAds.sharedInstance().applicationVolume = call.getFloat("volume")!
        }

        if call.getFloat("muted") != nil {
            GADMobileAds.sharedInstance().applicationMuted = call.getBool("muted")!
        }

    }

    /**
     *  AdMob: Banner
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/banner?hl=ja
     */
    @objc func showBanner(_ call: CAPPluginCall) {
        let adUnitID = getAdId(call, "ca-app-pub-3940256099942544/6300978111")
        let request = self.GADRequestWithOption(call.getBool("npa") ?? false)

        DispatchQueue.main.async {
            self.bannerExecutor.showBanner(call, request, adUnitID)
        }
    }

    @objc func hideBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.bannerExecutor.hideBanner(call)
        }
    }

    @objc func resumeBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.bannerExecutor.resumeBanner(call)
        }
    }

    @objc func removeBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.bannerExecutor.removeBanner(call)
        }
    }

    /**
     *  AdMob: Intertitial
     *  https://developers.google.com/admob/ios/interstitial?hl=ja
     */
    @objc func prepareInterstitial(_ call: CAPPluginCall) {
        let adUnitID = getAdId(call, "ca-app-pub-3940256099942544/1033173712")
        let request = self.GADRequestWithOption(call.getBool("npa") ?? false)

        DispatchQueue.main.async {
            self.adInterstitialExecutor.prepareInterstitial(call, request, adUnitID)
        }
    }

    @objc func showInterstitial(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.adInterstitialExecutor.showInterstitial(call)
        }
    }

    /**
     *  AdMob: Rewarded Ads
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/rewarded-ads?hl=ja
     */
    @objc func prepareRewardVideoAd(_ call: CAPPluginCall) {
        let adUnitID = getAdId(call, "ca-app-pub-3940256099942544/1712485313")
        let request = self.GADRequestWithOption(call.getBool("npa") ?? false)

        DispatchQueue.main.async {
            self.adRewardExecutor.prepareRewardVideoAd(call, request, adUnitID)
        }
    }

    @objc func showRewardVideoAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.adRewardExecutor.showRewardVideoAd(call)
        }
    }

    private func getAdId(_ call: CAPPluginCall, _ testingID: String) -> String {
        let adUnitID = call.getString("adId") ?? testingID
        let isTest = call.getBool("isTesting") ?? false
        if isTest {
            return testingID
        }
        return adUnitID
    }

    private func GADRequestWithOption(_ npa: Bool) -> GADRequest {
        let request = GADRequest()

        if npa {
            let extras = GADExtras()
            extras.additionalParameters = ["npa": "1"]
            request.register(extras)
        }

        return request
    }
}
