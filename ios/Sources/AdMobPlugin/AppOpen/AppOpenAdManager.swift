import Foundation
import Capacitor
import GoogleMobileAds
import UIKit

@objc public class AppOpenAdManager: NSObject {
    private var appOpenAd: AppOpenAd?
    private var webViewLayout: FullScreenAdWebViewLayout?
    private var expiration: AdExpiration?
    private var isLoadingAd = false
    private var isShowingAd = false
    private var adUnitId: String
    private var onOpened: (() -> Void)?

    public init(adUnitId: String) {
        self.adUnitId = adUnitId
    }

    public func loadAd(onLoaded: @escaping () -> Void, onFailed: @escaping (Error?) -> Void, onPaidEvent: @escaping (_ valueMicros: Int64, _ currencyCode: String, _ precision: Int, _ networkName: String, _ impressionId: String) -> Void) {
        if isAdLoaded() {
            onLoaded()
            return
        }

        if isShowingAd {
            onFailed(nil)
            return
        }

        if isLoadingAd {
            onFailed(nil)
            return
        }

        appOpenAd = nil
        expiration = nil
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
                    self.expiration = AdExpiration(maxAge: 4 * 3600)
                    ad.paidEventHandler = { adValue in
                        let networkName = ad.responseInfo.loadedAdNetworkResponseInfo?.adNetworkClassName ?? ""
                        let impressionId = ad.responseInfo.responseIdentifier ?? ""
                        onPaidEvent(
                            adValue.value.multiplying(byPowerOf10: 6).int64Value,
                            adValue.currencyCode,
                            adValue.precision.rawValue,
                            networkName,
                            impressionId
                        )
                    }
                    onLoaded()
                }
            } catch {
                await MainActor.run {
                    self.isLoadingAd = false
                    onFailed(error)
                }
            }
        }
    }

    public func showAdIfAvailable(
        rootViewController: UIViewController,
        onOpened: @escaping () -> Void,
        onClosed: @escaping () -> Void,
        onFailedToShow: @escaping (Error?) -> Void
    ) {
        guard let ad = appOpenAd, isAdLoaded(), !isShowingAd else {
            onFailedToShow(nil)
            return
        }

        self.onOpened = onOpened
        self.onClosed = onClosed
        self.onFailedToShow = onFailedToShow
        isShowingAd = true
        ad.fullScreenContentDelegate = self
        webViewLayout = FullScreenAdWebViewLayout(webView: (rootViewController as? CAPBridgeViewController)?.webView)
        ad.present(from: rootViewController)
    }

    public func isAdLoaded() -> Bool {
        return appOpenAd != nil && expiration?.isExpired == false
    }

    private var onClosed: (() -> Void)?
    private var onFailedToShow: ((Error?) -> Void)?
}

extension AppOpenAdManager: FullScreenContentDelegate {
    public func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        let onOpened = self.onOpened
        self.onOpened = nil
        onOpened?()
    }

    public func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        appOpenAd = nil
        expiration = nil
        isShowingAd = false
        let layout = webViewLayout
        webViewLayout = nil
        DispatchQueue.main.async { layout?.restore() }
        let onClosed = self.onClosed
        self.onOpened = nil
        self.onClosed = nil
        self.onFailedToShow = nil
        onClosed?()
    }

    public func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        appOpenAd = nil
        expiration = nil
        isShowingAd = false
        let layout = webViewLayout
        webViewLayout = nil
        DispatchQueue.main.async { layout?.restore() }
        let onFailedToShow = self.onFailedToShow
        self.onOpened = nil
        self.onClosed = nil
        self.onFailedToShow = nil
        onFailedToShow?(error)
    }
}
