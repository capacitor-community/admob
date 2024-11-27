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
    private let adRewardInterstitialExecutor = AdRewardInterstitialExecutor()
    private let consentExecutor = ConsentExecutor()

    /**
     * Enable SKAdNetwork to track conversions
     * https://developers.google.com/admob/ios/ios14
     */
    @objc func initialize(_ call: CAPPluginCall) {
        self.bannerExecutor.plugin = self
        self.adInterstitialExecutor.plugin = self
        self.adRewardExecutor.plugin = self
        self.adRewardInterstitialExecutor.plugin = self
        self.adInterstitialExecutor.plugin = self
        self.consentExecutor.plugin = self
        self.setRequestConfiguration(call)

        GADMobileAds.sharedInstance().start(completionHandler: nil)
        call.resolve([:])
    }

    /**
     * DEPRECATED: It's now ship with Admob UMP Consent
     */
    @objc func requestTrackingAuthorization(_ call: CAPPluginCall) {
        if #available(iOS 14, *) {
            #if canImport(AppTrackingTransparency)
            ATTrackingManager.requestTrackingAuthorization(completionHandler: { _ in
                call.resolve([:])
            })
            #else
            call.resolve([:])
            #endif
        } else {
            call.resolve([:])
        }
    }

    @objc func setApplicationMuted(_ call: CAPPluginCall) {
        if let shouldMute = call.getBool("muted") {
            GADMobileAds.sharedInstance().applicationMuted = shouldMute
            call.resolve([:])
        } else {
            call.reject("muted property cannot be null")
            return
        }
    }

    @objc func setApplicationVolume(_ call: CAPPluginCall) {
        if var volume = call.getFloat("volume") {
            // Clamp volumes.
            if volume < 0.0 {volume = 0.0} else if volume > 1.0 {volume = 1.0}

            GADMobileAds.sharedInstance().applicationVolume = volume

            call.resolve([:])
        } else {
            call.reject("volume property cannot be null")
            return
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
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/rewarded-ads
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
    
    /**
     *  AdMob: Rewarded Interstitial Ads
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/rewarded-interstitial
     */
    @objc func prepareRewardInterstitialAd(_ call: CAPPluginCall) {
        let adUnitID = getAdId(call, "ca-app-pub-3940256099942544/6978759866")
        let request = self.GADRequestWithOption(call.getBool("npa") ?? false)

        DispatchQueue.main.async {
            self.adRewardInterstitialExecutor.prepareRewardInterstitialAd(call, request, adUnitID)
        }
    }

    @objc func showRewardInterstitialAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.adRewardInterstitialExecutor.showRewardInterstitialAd(call)
        }
    }

    @objc func trackingAuthorizationStatus(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if #available(iOS 14, *) {
                switch ATTrackingManager.trackingAuthorizationStatus {
                case .authorized:
                    call.resolve(["status": AuthorizationStatusEnum.Authorized.rawValue])
                    break
                case .denied:
                    call.resolve(["status": AuthorizationStatusEnum.Denied.rawValue])
                    break
                case .restricted:
                    call.resolve(["status": AuthorizationStatusEnum.Restricted.rawValue])
                    break
                case .notDetermined:
                    call.resolve(["status": AuthorizationStatusEnum.NotDetermined.rawValue])
                    break
                @unknown default:
                    call.reject("trackingAuthorizationStatus can't get status")
                }
            } else {
                call.resolve(["status": AuthorizationStatusEnum.Authorized])
            }
        }
    }

    /**
     * Admob: User Message Platform
     * https://support.google.com/admob/answer/10113005?hl=en
     */
    @objc func requestConsentInfo(_ call: CAPPluginCall) {
        let debugGeography = call.getInt("debugGeography", 0)

        let testDeviceJSArray = call.getArray("testDeviceIdentifiers") ?? []
        var testDeviceIdentifiers: [String] = []
        if testDeviceJSArray.count > 0 {
            for deviceId in testDeviceJSArray {
                if let name = deviceId as? String {
                    testDeviceIdentifiers.append(name)
                }
            }
        }

        let tagForUnderAgeOfConsent = call.getBool("tagForUnderAgeOfConsent", false)

        DispatchQueue.main.async {
            self.consentExecutor.requestConsentInfo(call, debugGeography, testDeviceIdentifiers, tagForUnderAgeOfConsent)
        }
    }

    @objc func showConsentForm(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.consentExecutor.showConsentForm(call)
        }
    }

    @objc func resetConsentInfo(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.consentExecutor.resetConsentInfo(call)
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

    /**
     * https://developers.google.com/admob/ios/targeting?hl=ja
     */
    private func setRequestConfiguration(_ call: CAPPluginCall) {

        if call.getBool("initializeForTesting") ?? false {
            GADMobileAds.sharedInstance().requestConfiguration.testDeviceIdentifiers = call.getArray("testingDevices", String.self) ?? []
        }

        if call.getBool("tagForChildDirectedTreatment") == true {
            GADMobileAds.sharedInstance().requestConfiguration.tagForChildDirectedTreatment = true
        }

        if call.getBool("tagForUnderAgeOfConsent") == true {
            GADMobileAds.sharedInstance().requestConfiguration.tagForUnderAgeOfConsent = true
        }

        if call.getString("maxAdContentRating") != nil {
            switch call.getString("maxAdContentRating") {
            case "General":
                GADMobileAds.sharedInstance().requestConfiguration.maxAdContentRating =
                    GADMaxAdContentRating.general
            case "ParentalGuidance":
                GADMobileAds.sharedInstance().requestConfiguration.maxAdContentRating =
                    GADMaxAdContentRating.parentalGuidance
            case "Teen":
                GADMobileAds.sharedInstance().requestConfiguration.maxAdContentRating =
                    GADMaxAdContentRating.teen
            case "MatureAudience":
                GADMobileAds.sharedInstance().requestConfiguration.maxAdContentRating =
                    GADMaxAdContentRating.matureAudience
            default:
                print("maxAdContentRating can't find value")
            }
        }

    }

    func getRootVC() -> UIViewController? {
        var window: UIWindow? = UIApplication.shared.delegate?.window ?? nil

        if window == nil {
            let scene: UIWindowScene? = UIApplication.shared.connectedScenes.first as? UIWindowScene
            window = scene?.windows.filter({$0.isKeyWindow}).first
            if window == nil {
                window = scene?.windows.first
            }
        }
        return window?.rootViewController
    }
}
