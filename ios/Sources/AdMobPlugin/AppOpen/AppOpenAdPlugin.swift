import Foundation
import Capacitor
import UIKit

@objc public class AppOpenAdPlugin: NSObject {
    private var appOpenAdManager: AppOpenAdManager?
    private var currentAdUnitId: String?

    @objc func loadAppOpen(
        _ call: CAPPluginCall,
        getRootViewController: @escaping () -> UIViewController?,
        notify: @escaping (String, [String: Any]) -> Void
    ) {
        guard let adUnitId = call.getString("adId") else {
            call.reject("adId is required")
            return
        }
        if appOpenAdManager == nil || currentAdUnitId != adUnitId {
            appOpenAdManager = AppOpenAdManager(adUnitId: adUnitId)
            currentAdUnitId = adUnitId
        }
        DispatchQueue.main.async {
            if let rootVC = getRootViewController() {
                self.appOpenAdManager?.loadAd(rootViewController: rootVC, onLoaded: {
                    notify("appOpenAdLoaded", [:])
                    call.resolve()
                }, onFailed: {
                    notify("appOpenAdFailedToLoad", [:])
                    call.reject("Failed to load App Open Ad")
                })
            } else {
                call.reject("No rootViewController")
            }
        }
    }

    @objc func showAppOpen(
        _ call: CAPPluginCall,
        getRootViewController: @escaping () -> UIViewController?,
        notify: @escaping (String, [String: Any]) -> Void
    ) {
        DispatchQueue.main.async {
            guard self.appOpenAdManager != nil else {
                call.reject("App Open Ad manager is not initialized")
                return
            }

            if let rootVC = getRootViewController() {
                self.appOpenAdManager?.showAdIfAvailable(rootViewController: rootVC, onOpened: {
                    notify("appOpenAdOpened", [:])
                }, onClosed: {
                    notify("appOpenAdClosed", [:])
                    call.resolve()
                }, onFailedToShow: {
                    notify("appOpenAdFailedToShow", [:])
                    call.reject("Failed to show App Open Ad")
                })
            } else {
                call.reject("No rootViewController")
            }
        }
    }

    @objc func isAppOpenLoaded(_ call: CAPPluginCall) {
        let loaded = appOpenAdManager?.isAdLoaded() ?? false
        call.resolve(["value": loaded])
    }
}
