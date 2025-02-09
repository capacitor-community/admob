import Foundation
import Capacitor
import GoogleMobileAds

class BannerExecutor: NSObject, GADBannerViewDelegate {
    weak var plugin: AdMobPlugin?
    var bannerView: GADBannerView!

    func showBanner(_ call: CAPPluginCall, _ request: GADRequest, _ adUnitID: String) {
        if let rootViewController = plugin?.getRootVC() {

            let adSize = call.getString("adSize") ?? "ADAPTIVE_BANNER"
            let adPosition = call.getString("position") ?? "BOTTOM_CENTER"
            let adMargin = call.getInt("margin") ?? 0

            var bannerSize: GADAdSize

            switch adSize {
            case "BANNER":
                bannerSize = GADAdSizeBanner
                break
            case "LARGE_BANNER":
                bannerSize = GADAdSizeLargeBanner
                break
            case "FULL_BANNER":
                bannerSize = GADAdSizeFullBanner
                break
            case "LEADERBOARD":
                bannerSize = GADAdSizeLeaderboard
                break
            case "MEDIUM_RECTANGLE":
                bannerSize = GADAdSizeMediumRectangle
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
            self.bannerView.adUnitID = adUnitID
            self.bannerView.rootViewController = plugin?.getRootVC()

            self.bannerView.load(request)
            self.bannerView.delegate = self

            call.resolve([:])
        }
    }

    func hideBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootViewController = self.plugin?.getRootVC() {
                if let subView = rootViewController.view.viewWithTag(2743243288699) {
                    NSLog("AdMob: find subView for hideBanner")
                    subView.isHidden = true
                } else {
                    NSLog("AdMob: not find subView for resumeBanner for hideBanner")
                }
            }

            self.plugin?.notifyListeners(BannerAdPluginEvents.SizeChanged.rawValue, data: [
                "width": 0,
                "height": 0
            ])

            call.resolve([:])
        }
    }

    func resumeBanner(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            if let subView = rootViewController.view.viewWithTag(2743243288699) {
                NSLog("AdMob: find subView for resumeBanner")
                subView.isHidden = false

                self.plugin?.notifyListeners(BannerAdPluginEvents.SizeChanged.rawValue, data: [
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

    func removeBanner(_ call: CAPPluginCall) {
        self.removeBannerViewToView()
        call.resolve([:])
    }

    private func addBannerViewToView(_ bannerView: GADBannerView, _ adPosition: String, _ Margin: Int) {
        removeBannerViewToView()
        if let rootViewController = plugin?.getRootVC() {

            bannerView.translatesAutoresizingMaskIntoConstraints = false
            bannerView.tag = 2743243288699 // rand
            rootViewController.view.addSubview(bannerView)
            rootViewController.view.addConstraints(
                [NSLayoutConstraint(item: bannerView,
                                    attribute: adPosition == "TOP_CENTER" ? .top : .bottom,
                                    relatedBy: .equal,
                                    toItem: rootViewController.view.safeAreaLayoutGuide,
                                    attribute: adPosition == "TOP_CENTER" ? .top : .bottom,
                                    multiplier: 1,
                                    constant: CGFloat(Int(Margin) * -1)),
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
        if let rootViewController = plugin?.getRootVC() {
            if let subView = rootViewController.view.viewWithTag(2743243288699) {
                bannerView.delegate = nil
                NSLog("AdMob: find subView")
                subView.removeFromSuperview()
            }
        }
    }

    /// Tells the delegate an ad request loaded an ad.
    func bannerViewDidReceiveAd(_ bannerView: GADBannerView) {
        NSLog("bannerViewDidReceiveAd")

        self.plugin?.notifyListeners(BannerAdPluginEvents.SizeChanged.rawValue, data: [
            "width": bannerView.frame.width,
            "height": bannerView.frame.height
        ])
        self.plugin?.notifyListeners(BannerAdPluginEvents.Loaded.rawValue, data: [:])
    }

    /// Tells the delegate an ad request failed.
    func bannerView(_ bannerView: GADBannerView,
                    didFailToReceiveAdWithError error: Error) {
        NSLog("bannerView:didFailToReceiveAdWithError: \(error.localizedDescription)")
        self.removeBannerViewToView()
        self.plugin?.notifyListeners(BannerAdPluginEvents.SizeChanged.rawValue, data: [
            "width": 0,
            "height": 0
        ])
        self.plugin?.notifyListeners(BannerAdPluginEvents.FailedToLoad.rawValue, data: [
            "code": 0,
            "message": error.localizedDescription
        ])
    }

    func bannerViewDidRecordImpression(_ bannerView: GADBannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.AdImpression.rawValue, data: [:])
    }

    /// Tells the delegate that a full-screen view will be presented in response
    /// to the user clicking on an ad.
    func bannerViewWillPresentScreen(_ bannerView: GADBannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.Opened.rawValue, data: [:])
    }

    /// Tells the delegate that the full-screen view will be dismissed.
    func bannerViewWillDismissScreen(_ bannerView: GADBannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.Closed.rawValue, data: [:])
    }
}
