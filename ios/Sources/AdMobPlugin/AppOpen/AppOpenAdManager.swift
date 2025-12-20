import Foundation
import GoogleMobileAds
import UIKit

@objc public class AppOpenAdManager: NSObject {
    private var appOpenAd: GADAppOpenAd?
    private var isLoadingAd = false
    private var isShowingAd = false
    private var adUnitId: String

    public init(adUnitId: String) {
        self.adUnitId = adUnitId
    }

    public func loadAd(rootViewController: UIViewController, onLoaded: @escaping () -> Void, onFailed: @escaping () -> Void) {
        if isLoadingAd || appOpenAd != nil {
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

    public func showAdIfAvailable(rootViewController: UIViewController, onClosed: @escaping () -> Void, onFailedToShow: @escaping () -> Void) {
        guard let ad = appOpenAd, !isShowingAd else {
            onFailedToShow()
            return
        }

        isShowingAd = true
        ad.fullScreenContentDelegate = self
        ad.present(fromRootViewController: rootViewController)
        self.onClosed = onClosed
        self.onFailedToShow = onFailedToShow
    }

    public func isAdLoaded() -> Bool {
        return appOpenAd != nil
    }

    private var onClosed: (() -> Void)?
    private var onFailedToShow: (() -> Void)?
}

extension AppOpenAdManager: GADFullScreenContentDelegate {
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
