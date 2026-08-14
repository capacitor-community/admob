import Foundation
import Capacitor
import UIKit

@objc public class AppOpenAdPlugin: NSObject {
    private var preparedManagers: [String: AppOpenAdManager] = [:]
    private var lastPreparedAdId: String?

    @objc func loadAppOpen(
        _ call: CAPPluginCall,
        notify: @escaping (String, [String: Any]) -> Void
    ) {
        guard let adUnitId = call.getString("adId") else {
            call.reject("adId is required")
            return
        }

        DispatchQueue.main.async {
            if self.preparedManagers[adUnitId] == nil {
                self.preparedManagers[adUnitId] = AppOpenAdManager(adUnitId: adUnitId)
            }

            self.preparedManagers[adUnitId]?.loadAd(onLoaded: {
                self.lastPreparedAdId = adUnitId
                notify(AppOpenAdPluginEvents.Loaded.rawValue, ["adUnitId": adUnitId])
                call.resolve(["adUnitId": adUnitId])
            }, onFailed: { error in
                let message = error?.localizedDescription ?? "Failed to load App Open Ad"
                notify(AppOpenAdPluginEvents.FailedToLoad.rawValue, [
                    "code": 0,
                    "message": message
                ])
                call.reject(message)
            }, onPaidEvent: { valueMicros, currencyCode, precision, networkName, impressionId in
                notify(AppOpenAdPluginEvents.AdImpression.rawValue, [
                    "adUnitId": adUnitId,
                    "valueMicros": valueMicros,
                    "currencyCode": currencyCode,
                    "precision": precision,
                    "networkName": networkName,
                    "impressionId": impressionId
                ])
            })
        }
    }

    @objc func showAppOpen(
        _ call: CAPPluginCall,
        getRootViewController: @escaping () -> UIViewController?,
        notify: @escaping (String, [String: Any]) -> Void
    ) {
        guard let adId = call.getString("adId") ?? lastPreparedAdId else {
            call.reject("No ad prepared")
            return
        }

        DispatchQueue.main.async {
            guard let manager = self.preparedManagers[adId], manager.isAdLoaded() else {
                call.reject("App Open Ad is not loaded")
                return
            }

            if let rootVC = getRootViewController() {
                manager.showAdIfAvailable(rootViewController: rootVC, onOpened: {
                    notify(AppOpenAdPluginEvents.Opened.rawValue, [:])
                }, onClosed: {
                    self.preparedManagers.removeValue(forKey: adId)
                    if self.lastPreparedAdId == adId {
                        self.lastPreparedAdId = self.preparedManagers.first(where: { $0.value.isAdLoaded() })?.key
                    }
                    notify(AppOpenAdPluginEvents.Closed.rawValue, [:])
                    call.resolve()
                }, onFailedToShow: { error in
                    // A second show request while this ad is already presented fails
                    // without consuming the ad. Keep the manager alive so its weak
                    // full-screen delegate can still deliver the original close event.
                    if !manager.isAdLoaded() {
                        self.preparedManagers.removeValue(forKey: adId)
                        if self.lastPreparedAdId == adId {
                            self.lastPreparedAdId = self.preparedManagers.first(where: { $0.value.isAdLoaded() })?.key
                        }
                    }
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
        let adId = call.getString("adId") ?? lastPreparedAdId
        let loaded = adId.flatMap { preparedManagers[$0]?.isAdLoaded() } ?? false
        call.resolve(["value": loaded])
    }
}
