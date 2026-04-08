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
                }, onFailed: { error in
                    let message = error?.localizedDescription ?? "Failed to load App Open Ad"
                    notify("appOpenAdFailedToLoad", [
                        "code": 0,
                        "message": message
                    ])
                    call.reject(message)
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
            guard let manager = self.appOpenAdManager, manager.isAdLoaded() else {
                call.reject("App Open Ad is not loaded")
                return
            }

            if let rootVC = getRootViewController() {
                self.appOpenAdManager?.showAdIfAvailable(rootViewController: rootVC, onOpened: {
                    notify("appOpenAdOpened", [:])
                }, onClosed: {
                    notify("appOpenAdClosed", [:])
                    call.resolve()
                }, onFailedToShow: { error in
                    let message = error?.localizedDescription ?? "Failed to show App Open Ad"
                    notify("appOpenAdFailedToShow", [
                        "code": 0,
                        "message": message
                    ])
                    call.reject(message)
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
