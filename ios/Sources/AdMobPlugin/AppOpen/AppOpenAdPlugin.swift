import Foundation
import Capacitor
import UIKit

@objc public class AppOpenAdPlugin: NSObject {
    private var appOpenAdManager: AppOpenAdManager?
    private var currentAdUnitId: String?

    @objc func loadAppOpen(
        _ call: CAPPluginCall,
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
            self.appOpenAdManager?.loadAd(onLoaded: {
                notify(AppOpenAdPluginEvents.Loaded.rawValue, ["adUnitId": adUnitId])
                call.resolve(["adUnitId": adUnitId])
            }, onFailed: { error in
                let message = error?.localizedDescription ?? "Failed to load App Open Ad"
                notify(AppOpenAdPluginEvents.FailedToLoad.rawValue, [
                    "code": 0,
                    "message": message
                ])
                call.reject(message)
            })
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
                    notify(AppOpenAdPluginEvents.Opened.rawValue, [:])
                }, onClosed: {
                    notify(AppOpenAdPluginEvents.Closed.rawValue, [:])
                    call.resolve()
                }, onFailedToShow: { error in
                    let message = error?.localizedDescription ?? "Failed to show App Open Ad"
                    notify(AppOpenAdPluginEvents.FailedToShow.rawValue, [
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
