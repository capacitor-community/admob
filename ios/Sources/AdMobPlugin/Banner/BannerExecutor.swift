import Foundation
import Capacitor
import GoogleMobileAds

class BannerExecutor: NSObject, BannerViewDelegate {
    weak var plugin: AdMobPlugin?
    var bannerView: BannerView!
    var adPosition: String = ""
    var margin: Int = 0
    private var preparedBanner: PreparedBanner?

    private final class PreparedBanner: NSObject, BannerViewDelegate {
        weak var owner: BannerExecutor?
        let view: BannerView
        let adUnitID: String
        let sizeName: String
        let npa: Bool
        var pending: CAPPluginCall?

        init(owner: BannerExecutor, view: BannerView, call: CAPPluginCall, adUnitID: String) {
            self.owner = owner
            self.view = view
            self.adUnitID = adUnitID
            self.sizeName = call.getString("adSize") ?? "ADAPTIVE_BANNER"
            self.npa = call.getBool("npa") ?? false
            self.pending = call
        }

        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
            guard let owner = owner, owner.preparedBanner === self, let pending = pending else { return }
            self.pending = nil
            owner.plugin?.notifyListeners(BannerAdPluginEvents.Loaded.rawValue, data: [:])
            pending.resolve()
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
            guard let owner = owner, owner.preparedBanner === self else { return }
            owner.preparedBanner = nil
            view.delegate = nil
            pending?.reject(error.localizedDescription, String((error as NSError).code), error)
            pending = nil
            owner.plugin?.notifyListeners(BannerAdPluginEvents.FailedToLoad.rawValue, data: [
                "code": (error as NSError).code, "message": error.localizedDescription
            ])
        }
    }

    func prepareBanner(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        guard let root = plugin?.getRootVC(), let size = requestedBannerSize(call) else {
            call.reject("Banner view unavailable")
            return
        }
        discardPreparedBanner("Banner preparation was superseded by prepareBanner()")
        let view = BannerView(adSize: size)
        view.adUnitID = adUnitID
        view.rootViewController = root
        let prepared = PreparedBanner(owner: self, view: view, call: call, adUnitID: adUnitID)
        preparedBanner = prepared
        view.delegate = prepared
        view.load(request)
    }

    private func discardPreparedBanner(_ reason: String) {
        let prepared = preparedBanner
        preparedBanner = nil
        prepared?.view.delegate = nil
        prepared?.pending?.reject(reason)
        prepared?.pending = nil
    }

    private func takePreparedBanner(_ call: CAPPluginCall, adUnitID: String, size: AdSize) -> BannerView? {
        guard let prepared = preparedBanner else { return nil }
        guard prepared.pending == nil, prepared.adUnitID == adUnitID,
              prepared.sizeName == (call.getString("adSize") ?? "ADAPTIVE_BANNER"),
              prepared.npa == (call.getBool("npa") ?? false), prepared.view.adSize.size == size.size else {
            discardPreparedBanner("Banner preparation was superseded by showBanner()")
            return nil
        }
        preparedBanner = nil
        prepared.view.delegate = nil
        return prepared.view
    }

    func getAdaptiveBannerSize(_ call: CAPPluginCall) {
        let width = call.getDouble("width") ?? call.getInt("width").map(Double.init)
        if call.options["width"] != nil {
            guard let width = width, width.isFinite, width >= 1, width <= Double(Int32.max) else {
                call.reject("width must be a finite positive number of logical pixels")
                return
            }
        }
        guard let size = adaptiveBannerSize(width.map { CGFloat($0.rounded(.down)) }),
              size.size.width > 0, size.size.height > 0 else {
            call.reject("Unable to calculate an adaptive banner size for this width")
            return
        }
        call.resolve(["width": size.size.width, "height": size.size.height])
    }

    private func adaptiveBannerSize(_ width: CGFloat? = nil) -> AdSize? {
        if let width = width { return currentOrientationAnchoredAdaptiveBanner(width: width) }
        guard let view = plugin?.getRootVC()?.view else { return nil }
        view.layoutIfNeeded()
        let availableWidth = view.bounds.inset(by: view.safeAreaInsets).width
        return currentOrientationAnchoredAdaptiveBanner(width: availableWidth)
    }

    func getBannerSize(_ call: CAPPluginCall) {
        guard let banner = bannerView, banner.superview != nil, !banner.isHidden else {
            call.resolve(["width": 0, "height": 0])
            return
        }
        call.resolve(["width": banner.bounds.width, "height": banner.bounds.height])
    }

    func showBanner(_ call: CAPPluginCall, _ request: Request, _ adUnitID: String) {
        if let rootViewController = plugin?.getRootVC() {

            let adPosition = call.getString("position") ?? "BOTTOM_CENTER"
            let adMargin = call.getInt("margin") ?? 0
            guard let bannerSize = requestedBannerSize(call) else {
                call.reject("Banner view unavailable")
                return
            }
            let prepared = takePreparedBanner(call, adUnitID: adUnitID, size: bannerSize)

            var previousSize = CGSize.zero
            if prepared != nil {
                if let banner = self.bannerView, banner.superview != nil, !banner.isHidden {
                    previousSize = banner.bounds.size
                }
            }
            // A replaced banner that is still loading would otherwise add itself when
            // its ad arrives, or remove the new banner when its request fails.
            self.bannerView?.delegate = nil
            self.bannerView?.paidEventHandler = nil
            self.bannerView?.removeFromSuperview()
            self.bannerView = prepared ?? BannerView(adSize: bannerSize)
            self.addBannerViewToView(self.bannerView, adPosition, adMargin)
            self.bannerView.translatesAutoresizingMaskIntoConstraints = false
            self.bannerView.adUnitID = adUnitID
            self.bannerView.rootViewController = rootViewController

            if prepared == nil { self.bannerView.load(request) }
            self.bannerView.delegate = self

            self.bannerView.paidEventHandler = { [weak self] adValue in
                guard let self = self else { return }
                let networkName = self.bannerView.responseInfo?.loadedAdNetworkResponseInfo?.adNetworkClassName ?? ""
                let impressionId = self.bannerView.responseInfo?.responseIdentifier ?? ""
                self.plugin?.notifyListeners(BannerAdPluginEvents.AdPaid.rawValue, data: [
                    "adUnitId": self.bannerView.adUnitID ?? "",
                    "valueMicros": adValue.value.multiplying(byPowerOf10: 6).int64Value,
                    "currencyCode": adValue.currencyCode,
                    "precision": adValue.precision.rawValue,
                    "networkName": networkName,
                    "impressionId": impressionId
                ])
            }

            if prepared != nil { _ = displayBannerView(self.bannerView, previousSize: previousSize) }
            call.resolve([:])
        }
    }

    private func requestedBannerSize(_ call: CAPPluginCall) -> AdSize? {
        let adSize = call.getString("adSize") ?? "ADAPTIVE_BANNER"
        switch adSize {
        case "BANNER":
            return AdSizeBanner
        case "LARGE_BANNER":
            return AdSizeLargeBanner
        case "FULL_BANNER":
            return AdSizeFullBanner
        case "LEADERBOARD":
            return AdSizeLeaderboard
        case "MEDIUM_RECTANGLE":
            return AdSizeMediumRectangle
        case "SMART_BANNER":
            return kGADAdSizeSmartBannerPortrait
        default: // ADAPTIVE_BANNER
            return adaptiveBannerSize()
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
        discardPreparedBanner("Banner preparation was cancelled by removeBanner()")
        self.removeBannerViewToView()
        // A banner still loading, or one a failed refresh took off the
        // screen, has no tagged view to be found: without its delegate it
        // is never put up again.
        self.bannerView?.delegate = nil
        self.bannerView?.removeFromSuperview()
        call.resolve([:])
    }

    private func addBannerViewToView(_ bannerView: BannerView, _ adPosition: String, _ margin: Int) {
        removeBannerViewToView()
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        bannerView.tag = 2743243288699 // rand
        self.margin = margin
        self.adPosition = adPosition
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
    func bannerViewDidReceiveAd(_ bannerView: BannerView) {
        NSLog("bannerViewDidReceiveAd")
        if displayBannerView(bannerView) {
            self.plugin?.notifyListeners(BannerAdPluginEvents.Loaded.rawValue, data: [:])
        }
    }

    private func displayBannerView(_ bannerView: BannerView, previousSize: CGSize? = nil) -> Bool {
        if let rootViewController = plugin?.getRootVC() {
            // Each successful auto-refresh calls this again for the banner on screen. Keep
            // bringing it to the front, but add its constraints only once.
            if bannerView.superview === rootViewController.view {
                rootViewController.view.bringSubviewToFront(bannerView)
            } else {
                rootViewController.view.addSubview(bannerView)
                rootViewController.view.addConstraints(
                    [NSLayoutConstraint(item: bannerView,
                                        attribute: self.adPosition == "TOP_CENTER" ? .top : .bottom,
                                        relatedBy: .equal,
                                        toItem: rootViewController.view.safeAreaLayoutGuide,
                                        attribute: self.adPosition == "TOP_CENTER" ? .top : .bottom,
                                        multiplier: 1,
                                        constant: CGFloat(Int(self.margin) * -1)),
                     NSLayoutConstraint(item: bannerView,
                                        attribute: .centerX,
                                        relatedBy: .equal,
                                        toItem: rootViewController.view,
                                        attribute: .centerX,
                                        multiplier: 1,
                                        constant: 0)
                    ])
            }
            if previousSize != bannerView.frame.size {
                self.plugin?.notifyListeners(BannerAdPluginEvents.SizeChanged.rawValue, data: [
                    "width": bannerView.frame.width,
                    "height": bannerView.frame.height
                ])
            }
            return true

        }
        return false
    }

    /// Tells the delegate an ad request failed.
    func bannerView(_ bannerView: BannerView,
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

    func bannerViewDidRecordImpression(_ bannerView: BannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.AdImpression.rawValue, data: [:])
    }

    /// Tells the delegate that a full-screen view will be presented in response
    /// to the user clicking on an ad.
    func bannerViewWillPresentScreen(_ bannerView: BannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.Opened.rawValue, data: [:])
    }

    /// Tells the delegate that the full-screen view will be dismissed.
    func bannerViewWillDismissScreen(_ bannerView: BannerView) {
        self.plugin?.notifyListeners(BannerAdPluginEvents.Closed.rawValue, data: [:])
    }
}
