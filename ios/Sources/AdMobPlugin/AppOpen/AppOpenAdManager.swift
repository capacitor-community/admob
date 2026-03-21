import Foundation
import GoogleMobileAds
import UIKit

@objc public class AppOpenAdManager: NSObject {
    private var appOpenAd: AppOpenAd?
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
        Task { [weak self] in
            guard let self = self else {
                return
            }
            do {
                let ad = try await AppOpenAd.load(with: self.adUnitId, request: Request())
                await MainActor.run {
                    self.isLoadingAd = false
                    self.appOpenAd = ad
                    onLoaded()
                }
            } catch {
                await MainActor.run {
                    self.isLoadingAd = false
                    onFailed()
                }
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
        ad.present(from: rootViewController)
    }

    public func isAdLoaded() -> Bool {
        return appOpenAd != nil
    }

    private var onClosed: (() -> Void)?
    private var onFailedToShow: (() -> Void)?
}

extension AppOpenAdManager: FullScreenContentDelegate {
    public func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        onOpened?()
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        appOpenAd = nil
        isShowingAd = false
        onClosed?()
    }

    public func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        appOpenAd = nil
        isShowingAd = false
        onFailedToShow?()
    }
}
