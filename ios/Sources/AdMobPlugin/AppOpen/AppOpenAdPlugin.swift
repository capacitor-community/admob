import Foundation
import Capacitor
import UIKit

@objc(AppOpenAdPlugin)
public class AppOpenAdPlugin: CAPPlugin {
    private var appOpenAdManager: AppOpenAdManager?

    @objc func loadAppOpen(_ call: CAPPluginCall) {
        guard let adUnitId = call.getString("adUnitId") else {
            call.reject("adUnitId is required")
            return
        }
        if appOpenAdManager == nil {
            appOpenAdManager = AppOpenAdManager(adUnitId: adUnitId)
        }
        DispatchQueue.main.async {
            if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
                self.appOpenAdManager?.loadAd(rootViewController: rootVC, onLoaded: {
                    self.notifyListeners("appOpenAdLoaded", data: [:])
                    call.resolve()
                }, onFailed: {
                    self.notifyListeners("appOpenAdFailedToLoad", data: [:])
                    call.reject("Failed to load App Open Ad")
                })
            } else {
                call.reject("No rootViewController")
            }
        }
    }

    @objc func showAppOpen(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
                self.appOpenAdManager?.showAdIfAvailable(rootViewController: rootVC, onClosed: {
                    self.notifyListeners("appOpenAdClosed", data: [:])
                    call.resolve()
                }, onFailedToShow: {
                    self.notifyListeners("appOpenAdFailedToShow", data: [:])
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
