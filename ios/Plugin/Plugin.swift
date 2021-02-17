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
public class AdMob: CAPPlugin, GADBannerViewDelegate, GADFullScreenContentDelegate, GADRewardedAdDelegate, GADInterstitialDelegate {

    var bannerView: GADBannerView!
    var interstitial: GADInterstitial!
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
            call.success([
                "value": true
            ])
        } else if #available(iOS 14, *) {
            #if canImport(AppTrackingTransparency)
            ATTrackingManager.requestTrackingAuthorization(completionHandler: { _ in
                // iOS >= 14
                GADMobileAds.sharedInstance().start(completionHandler: nil)
                call.success([
                    "value": true
                ])

            })
            #else
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            call.success([
                "value": true
            ])
            #endif
        } else {
            // iOS < 14
            GADMobileAds.sharedInstance().start(completionHandler: nil)
            call.success([
                "value": true
            ])
        }
    }

    /**
     *  AdMob: Banner
     *  https://developers.google.com/ad-manager/mobile-ads-sdk/ios/banner?hl=ja
     */
    @objc func showBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let testingID = "ca-app-pub-3940256099942544/6300978111"
            var adId = call.getString("adId") ?? testingID
            let isTest = call.getBool("isTesting") ?? false
            if isTest {
                adId = testingID
            }

            let adSize = call.getString("adSize") ?? "SMART_BANNER"
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
            default:
                bannerSize = kGADAdSizeSmartBannerPortrait
                break
            }

            self.bannerView = GADBannerView(adSize: bannerSize)
            self.addBannerViewToView(self.bannerView, adPosition, adMargin)
            self.bannerView.translatesAutoresizingMaskIntoConstraints = false
            self.bannerView.adUnitID = adId
            self.bannerView.rootViewController = UIApplication.shared.keyWindow?.rootViewController

            self.bannerView.load(self.GADRequestWithOption(call.getBool("npa") ?? false))
            self.bannerView.delegate = self

            call.success([
                "value": true
            ])
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

            call.success([
                "value": true
            ])
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

                    call.success([
                        "value": true
                    ])

                } else {
                    NSLog("AdMob: not find subView for resumeBanner")

                    call.success([
                        "value": false
                    ])
                }
            }
        }
    }

    @objc func removeBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.removeBannerViewToView()
            call.success([
                "value": true
            ])
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
    public func adViewDidReceiveAd(_ bannerView: GADBannerView) {
        NSLog("adViewDidReceiveAd")
        self.notifyListeners("onAdSize", data: [
            "width": self.bannerView.frame.width,
            "height": self.bannerView.frame.height
        ])
        self.bridge?.triggerJSEvent(eventName: "adViewDidReceiveAd", target: "window")
    }

    /// Tells the delegate an ad request failed.
    public func adView(_ bannerView: GADBannerView,
                       didFailToReceiveAdWithError error: NSError) {
        NSLog("adView:didFailToReceiveAdWithError: \(error.localizedDescription)")
        self.removeBannerViewToView()
        self.notifyListeners("onAdSize", data: [
            "width": 0,
            "height": 0
        ])
        self.bridge?.triggerJSEvent(eventName: "adView:didFailToReceiveAdWithError", target: "window")
    }

    /// Tells the delegate that a full-screen view will be presented in response
    /// to the user clicking on an ad.
    public func adViewWillPresentScreen(_ bannerView: GADBannerView) {
        NSLog("adViewWillPresentScreen")
        self.bridge?.triggerJSEvent(eventName: "adViewWillPresentScreen", target: "window")
    }

    /// Tells the delegate that the full-screen view will be dismissed.
    public func adViewWillDismissScreen(_ bannerView: GADBannerView) {
        NSLog("adViewWillDismissScreen")
        self.bridge?.triggerJSEvent(eventName: "adViewWillDismissScreen", target: "window")
    }

    /// Tells the delegate that the full-screen view has been dismissed.
    public func adViewDidDismissScreen(_ bannerView: GADBannerView) {
        NSLog("adViewDidDismissScreen")
        self.bridge?.triggerJSEvent(eventName: "adViewDidDismissScreen", target: "window")
    }

    /// Tells the delegate that a user click will open another app (such as
    /// the App Store), backgrounding the current app.
    public func adViewWillLeaveApplication(_ bannerView: GADBannerView) {
        NSLog("adViewWillLeaveApplication")
        self.bridge?.triggerJSEvent(eventName: "adViewWillLeaveApplication", target: "window")
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
 
            self.interstitial = GADInterstitial(adUnitID: adUnitID)
            let request = self.GADRequestWithOption(call.getBool("npa") ?? false)
            self.interstitial.load(request)
            self.interstitial.delegate = self
            call.resolve(["value": true])
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

            call.resolve(["value": true])
        }
    }

    // Intertitial Events Degigates
    /// Tells the delegate an ad request succeeded.
    public func interstitialDidReceiveAd(_ ad: GADInterstitial) {
        NSLog("interstitialDidReceiveAd")
        self.notifyListeners("onInterstitialAdLoaded", data: ["value": true])
    }

    /// Tells the delegate an ad request failed.
    public func interstitial(_ ad: GADInterstitial, didFailToReceiveAdWithError error: NSError) {
        NSLog("interstitial:didFailToReceiveAdWithError: \(error.localizedDescription)")
        self.notifyListeners("onInterstitialAdFailedToLoad", data: ["error": error.localizedDescription, "errorCode": error.code])
    }

    public func adDidPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("Ad did present full screen content.")
        self.notifyListeners("onInterstitialAdOpened", data: ["value": true])
    }

    public func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        NSLog("Ad failed to present full screen content with error \(error.localizedDescription).")
    }

    public func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        NSLog("Ad did dismiss full screen content.")
        self.notifyListeners("onInterstitialAdClosed", data: ["value": true])
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
            self.rewardedAd = GADRewardedAd(adUnitID: adUnitID)
            self.rewardedAd?.load(request) { error in
              if let error = error {
                // Handle ad failed to load case.
                NSLog("Rewarded ad failed to load with error: \(error.localizedDescription)")
                call.reject("Loading failed")
                return
              } else {
                // Ad successfully loaded.
                NSLog("AdMob Reward: Loading Succeeded")
                self.notifyListeners("onRewardedVideoAdLoaded", data: ["value": true])
                call.resolve(["value": true])
              }
            }
        }
    }

    @objc func showRewardVideoAd(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                if self.rewardedAd?.isReady == true {
                    self.rewardedAd?.present(fromRootViewController: rootViewController, delegate:self)
                    call.resolve([ "value": true ])
                } else {
                    call.reject("Reward Video is Not Ready Yet")
                }
            }

        }
    }

    /// Tells the delegate that the rewarded ad was presented.
    public func rewardedAdDidPresent(_ rewardedAd: GADRewardedAd) {
        NSLog("AdMob Reward ad presented.")
        self.notifyListeners("onRewardedVideoAdOpened", data: ["value": true])
        self.notifyListeners("onRewardedVideoStarted", data: ["value": true])
    }
    /// Tells the delegate that the user earned a reward.
    public func rewardedAd(_ rewardedAd: GADRewardedAd, userDidEarn reward: GADAdReward) {
        NSLog("AdMob Reward received with currency: \(reward.type), amount \(reward.amount).")
        self.notifyListeners("onRewarded", data: ["value": true, "type": reward.type, "amount": reward.amount])

        // todo: Capacitor3で整理
        self.notifyListeners("onRewardedVideoCompleted", data: ["value": true, "type": reward.type, "amount": reward.amount])
    }
    /// Tells the delegate that the rewarded ad was dismissed.
    public func rewardedAdDidDismiss(_ rewardedAd: GADRewardedAd) {
        NSLog("AdMob Reward ad dismissed.")
        self.notifyListeners("onRewardedVideoAdClosed", data: ["value": true])
        self.notifyListeners("onRewardedVideoAdLeftApplication", data: ["value": true])
    }
    /// Tells the delegate that the rewarded ad failed to present.
    public func rewardedAd(_ rewardedAd: GADRewardedAd, didFailToPresentWithError error: Error) {
        NSLog("AdMob Reward ad failed to present.")
        self.notifyListeners("onRewardedVideoAdFailedToLoad", data: ["error": error.localizedDescription])
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
