import Foundation
import GoogleMobileAds
import UIKit

@objc public class AppOpenAdManager: NSObject {
    private var appOpenAd: GADAppOpenAd?
    private var isLoadingAd = false
    private var isShowingAd = false
    private var adUnitId: String
    private var onOpened: (() -> Void)?

    public init(adUnitId: String) {
        self.adUnitId = adUnitId
    }

    public func loadAd(rootViewController: UIViewController, onLoaded: @escaping () -> Void, onFailed: @escaping () -> Void) {
        if appOpenAd != nil {
            onLoaded()
            return
        }

        if isLoadingAd {
            onFailed()
            return
        }

        isLoadingAd = true
        GADAppOpenAd.load(withAdUnitID: adUnitId, request: GADRequest(), orientation: .portrait) { [weak self] ad, error in
            self?.isLoadingAd = false

            if let ad = ad {
                self?.appOpenAd = ad
                onLoaded()
            } else {
                onFailed()
            }
        }
    }

    public func showAdIfAvailable(
        rootViewController: UIViewController,
        onOpened: @escaping () -> Void,
        onClosed: @escaping () -> Void,
        onFailedToShow: @escaping () -> Void
    ) {
        guard let ad = appOpenAd, !isShowingAd else {
            onFailedToShow()
            return
        }

        self.onOpened = onOpened
        self.onClosed = onClosed
        self.onFailedToShow = onFailedToShow
        isShowingAd = true
        ad.fullScreenContentDelegate = self
        ad.present(fromRootViewController: rootViewController)
    }

    public func isAdLoaded() -> Bool {
        return appOpenAd != nil
    }

    private var onClosed: (() -> Void)?
    private var onFailedToShow: (() -> Void)?
}

extension AppOpenAdManager: GADFullScreenContentDelegate {
    public func adWillPresentFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        onOpened?()
    }

    public func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        appOpenAd = nil
        isShowingAd = false
        onClosed?()
    }

    public func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        appOpenAd = nil
        isShowingAd = false
        onFailedToShow?()
    }
}
