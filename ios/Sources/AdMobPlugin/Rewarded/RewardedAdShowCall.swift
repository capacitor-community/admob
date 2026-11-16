import Foundation
import Capacitor

final class RewardedAdShowCall {
    let ad: AnyObject
    let adId: String
    private var resolve: (([String: Any]) -> Void)?
    private var reject: ((String, String) -> Void)?
    private var completed = false

    init(ad: AnyObject, adId: String, resolve: @escaping ([String: Any]) -> Void,
         reject: @escaping (String, String) -> Void) {
        self.ad = ad
        self.adId = adId
        self.resolve = resolve
        self.reject = reject
    }

    convenience init(ad: AnyObject, adId: String, call: CAPPluginCall) {
        self.init(ad: ad, adId: adId, resolve: { call.resolve($0) }, reject: { call.reject($0, $1) })
    }

    func reward(_ item: [String: Any]) -> Bool {
        guard !completed, let resolve = resolve else { return false }
        self.resolve = nil
        reject = nil
        resolve(item)
        return true
    }

    @discardableResult
    func finish(error: NSError? = nil) -> Bool {
        guard !completed else { return false }
        completed = true
        let reject = reject
        self.resolve = nil
        self.reject = nil
        if let error = error {
            reject?(error.localizedDescription, String(error.code))
        } else {
            reject?("Rewarded ad dismissed before earning a reward.", "REWARDED_AD_CANCELLED")
        }
        return true
    }
}
