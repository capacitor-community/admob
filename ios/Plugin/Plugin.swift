import Foundation
import Capacitor
import GoogleMobileAds
#if canImport(AppTrackingTransparency)
import AppTrackingTransparency
#endif

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(AdMob)
public class AdMob: CAPPlugin, GADBannerViewDelegate, GADFullScreenContentDelegate {

    var bannerView: GADBannerView!
    var interstitial: GADInterstitialAd!
    var rewardedAd: GADRewardedAd!
    var testingDevices: [String] = []

    /**
     * Enable SKAdNetwork to track conversions: https://developers.google.com/admob/ios/ios14
     */
    @objc func initialize(_ call: CAPPluginCall) {
        let isTrack = call.getBool("requestTrackingAuthorization") ?? true

        if call.getBool("initializeForTesting") ?? false {
            GADMobileAds.sharedInstance().requestConfiguration.testDeviceIdentifiers = call.getArray("testingDevices", String.self) ?? []
        }

        if !isTrack {
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            call.resolve([:])
        } else if #available(iOS 14, *) {
            #if canImport(AppTrackingTransparency)
            ATTrackingManager.requestTrackingAuthorization(completionHandler: { _ in
                // iOS >= 14
                GADMobileAds.sharedInstance().start(completionHandler: nil)
                call.resolve([:])

            })
            #else
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            call.resolve([:])
            #endif
        } else {
            // iOS < 14
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            call.resolve([:])
        }
    }

    /**
     *  AdMob: Banner
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/banner?hl=ja
     */
    @objc func showBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                let testingID = "ca-app-pub-3940256099942544/6300978111"
                var adId = call.getString("adId") ?? testingID
                let isTest = call.getBool("isTesting") ?? false
                if isTest {
                    adId = testingID
                }

                let adSize = call.getString("adSize") ?? "ADAPTIVE_BANNER"
                let adPosition = call.getString("position") ?? "BOTTOM_CENTER"
                let adMargin = call.getInt("margin") ?? 0
                var bannerSize = kGADAdSizeBanner

                switch adSize {
                case "BANNER":
                    bannerSize = kGADAdSizeBanner
                    break
                case "FLUID":
                    bannerSize = kGADAdSizeSmartBannerPortrait
                    break
                case "FULL_BANNER":
                    bannerSize = kGADAdSizeFullBanner
                    break
                case "LARGE_BANNER":
                    bannerSize = kGADAdSizeLargeBanner
                    break
                case "LEADERBOARD":
                    bannerSize = kGADAdSizeLeaderboard
                    break
                case "MEDIUM_RECTANGLE":
                    bannerSize = kGADAdSizeMediumRectangle
                    break
                case "SMART_BANNER":
                    bannerSize = kGADAdSizeSmartBannerPortrait
                    break
                default: // ADAPTIVE_BANNER
                    let frame = { () -> CGRect in
                        // Here safe area is taken into account, hence the view frame is used
                        // after the view has been laid out.
                        if #available(iOS 11.0, *) {
                            return rootViewController.view.frame.inset(by: rootViewController.view.safeAreaInsets)
                        } else {
                            return rootViewController.view.frame
                        }
                    }()
                    let viewWidth = frame.size.width
                    bannerSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth)
                    break
                }

                self.bannerView = GADBannerView(adSize: bannerSize)
                self.addBannerViewToView(self.bannerView, adPosition, adMargin)
                self.bannerView.translatesAutoresizingMaskIntoConstraints = false
                self.bannerView.adUnitID = adId
                self.bannerView.rootViewController = UIApplication.shared.keyWindow?.rootViewController

                self.bannerView.load(self.GADRequestWithOption(call.getBool("npa") ?? false))
                self.bannerView.delegate = self

                call.resolve([:])
            }
        }
    }

    @objc func hideBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                if let subView = rootViewController.view.viewWithTag(2743243288699) {
                    NSLog("AdMob: find subView for hideBanner")
                    subView.isHidden = true
                } else {
                    NSLog("AdMob: not find subView for resumeBanner for hideBanner")
                }
            }

            self.notifyListeners("onAdSize", data: [
                "width": 0,
                "height": 0
            ])

            call.resolve([:])
        }
    }

    @objc func resumeBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                if let subView = rootViewController.view.viewWithTag(2743243288699) {
                    NSLog("AdMob: find subView for resumeBanner")
                    subView.isHidden = false

                    self.notifyListeners("onAdSize", data: [
                        "width": subView.frame.width,
                        "height": subView.frame.height
                    ])

                    call.resolve([:])

                } else {
                    NSLog("AdMob: not find subView for resumeBanner")
                    call.reject("AdMob: not find subView for resumeBanner")
                }
            }
        }
    }

    @objc func removeBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.removeBannerViewToView()
            call.resolve([:])
        }
    }

    private func addBannerViewToView(_ bannerView: GADBannerView, _ adPosition: String, _ Margin: Int) {
        removeBannerViewToView()
        if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
            NSLog("AdMob: rendering rootView")

            var toItem = rootViewController.bottomLayoutGuide
            var adMargin = Int(Margin)

            switch adPosition {
            case "CENTER":
                // todo: position center
                toItem = rootViewController.bottomLayoutGuide
                adMargin = adMargin * -1
                break
            default:
                toItem = rootViewController.bottomLayoutGuide
                adMargin = adMargin * -1
                break
            }
            bannerView.translatesAutoresizingMaskIntoConstraints = false
            bannerView.tag = 2743243288699 // rand
            rootViewController.view.addSubview(bannerView)
            rootViewController.view.addConstraints(
                [NSLayoutConstraint(item: bannerView,
                                    attribute: adPosition == "TOP_CENTER" ? .top : .bottom,
                                    relatedBy: .equal,
                                    toItem: adPosition == "TOP_CENTER" ? rootViewController.view.safeAreaLayoutGuide : toItem,
                                    attribute: .top,
                                    multiplier: 1,
                                    constant: CGFloat(adMargin)),
                 NSLayoutConstraint(item: bannerView,
                                    attribute: .centerX,
                                    relatedBy: .equal,
                                    toItem: rootViewController.view,
                                    attribute: .centerX,
                                    multiplier: 1,
                                    constant: 0)
                ])
        }
    }

    private func removeBannerViewToView() {
        if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
            if let subView = rootViewController.view.viewWithTag(2743243288699) {
                NSLog("AdMob: find subView")
                subView.removeFromSuperview()
            }
        }
    }

    /// Tells the delegate an ad request loaded an ad.
    public func bannerViewDidReceiveAd(_ bannerView: GADBannerView) {
        NSLog("bannerViewDidReceiveAd")
        self.notifyListeners("bannerViewRecieveAdSize", data: [
            "width": bannerView.frame.width,
            "height": bannerView.frame.height
        ])
        self.bridge?.triggerJSEvent(eventName: "bannerViewDidReceiveAd", target: "window")
    }

    /// Tells the delegate an ad request failed.
    public func bannerView(_ bannerView: GADBannerView,
                           didFailToReceiveAdWithError error: Error) {
        NSLog("bannerView:didFailToReceiveAdWithError: \(error.localizedDescription)")
        self.removeBannerViewToView()
        self.notifyListeners("bannerViewRecieveAdSize", data: [
            "width": 0,
            "height": 0
        ])
        self.bridge?.triggerJSEvent(eventName: "bannerView:didFailToReceiveAdWithError", target: "window")
    }

    /// Tells the delegate that a full-screen view will be presented in response
    /// to the user clicking on an ad.
    public func bannerViewWillPresentScreen(_ bannerView: GADBannerView) {
        NSLog("bannerViewWillPresentScreen")
        self.bridge?.triggerJSEvent(eventName: "bannerViewWillPresentScreen", target: "window")
    }

    /// Tells the delegate that the full-screen view will be dismissed.
    public func bannerViewWillDismissScreen(_ bannerView: GADBannerView) {
        NSLog("bannerViewWillDismissScreen")
        self.bridge?.triggerJSEvent(eventName: "bannerViewWillDismissScreen", target: "window")
    }

    /// Tells the delegate that the full-screen view has been dismissed.
    public func bannerViewDidDismissScreen(_ bannerView: GADBannerView) {
        NSLog("bannerViewDidDismissScreen")
        self.bridge?.triggerJSEvent(eventName: "bannerViewDidDismissScreen", target: "window")
    }

    /**
     *  AdMob: Intertitial
     *  https://developers.google.com/admob/ios/interstitial?hl=ja
     */
    @objc func prepareInterstitial(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let testingID = "ca-app-pub-3940256099942544/1033173712"
            var adUnitID = call.getString("adId") ?? testingID
            let isTest = call.getBool("isTesting") ?? false
            if isTest {
                adUnitID = testingID
            }

            let request = self.GADRequestWithOption(call.getBool("npa") ?? false)
            GADInterstitialAd.load(
                withAdUnitID: adUnitID,
                request: request,
                completionHandler: { (ad, error) in
                    if error != nil {
                        NSLog("Received error on loading interstatial ad")
                        call.reject("Loading failed")
                        return
                    }

                    self.interstitial = ad
                    self.interstitial.fullScreenContentDelegate = self
                    call.resolve([:])
                }
            )
        }
    }

    @objc func showInterstitial(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                if let ad = self.interstitial {
                    ad.present(fromRootViewController: rootViewController)
                } else {
                    NSLog("Ad wasn't ready")
                }
            }

            call.resolve([:])
        }
    }

    /**
     *  AdMob: Rewarded Ads
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/rewarded-ads?hl=ja
     */
    @objc func prepareRewardVideoAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let testingID = "ca-app-pub-3940256099942544/1712485313"
            var adUnitID: String = call.getString("adId") ?? testingID
            let isTest = call.getBool("isTesting") ?? false
            if isTest {
                adUnitID = testingID
            }

            let request = self.GADRequestWithOption(call.getBool("npa") ?? false)

            GADRewardedAd.load(
                withAdUnitID: adUnitID,
                request: request,
                completionHandler: { (ad, error) in
                    if let error = error {
                        NSLog("Rewarded ad failed to load with error: \(error.localizedDescription)")
                        call.reject("Loading failed")
                        return
                    }

                    NSLog("AdMob Reward: Loading Succeeded")

                    self.rewardedAd = ad
                    self.rewardedAd?.fullScreenContentDelegate = self
                    call.resolve([:])
                }
            )
        }
    }

    @objc func showRewardVideoAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                if let ad = self.rewardedAd {
                    ad.present(fromRootViewController: rootViewController,
                               userDidEarnRewardHandler: {
                                let reward = ad.adReward
                                call.resolve(["type": reward.type, "amount": reward.amount])
                               }
                    )
                } else {
                    call.reject("Reward Video is Not Ready Yet")
                }
            }

        }
    }

    public func adDidPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("Ad did present full screen content.")
        self.notifyListeners("adDidPresentFullScreenContent", data: [:])
    }

    public func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("Ad failed to present full screen content with error \(error.localizedDescription).")
        self.notifyListeners("didFailToPresentFullScreenContentWithError", data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    public func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("Ad did dismiss full screen content.")
        self.notifyListeners("adDidDismissFullScreenContent", data: [:])
    }

    private func GADRequestWithOption(_ npa: Bool) -> GADRequest {
        let request = GADRequest()

        if npa {
            let extras = GADExtras()
            extras.additionalParameters = ["npa": "1"]
            request.register(extras)
            NSLog("AdMob don't use Personalize Adsense.")
        }

        return request
    }
}
