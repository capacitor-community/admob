import Capacitor
import Foundation
import GoogleMobileAds
import UIKit

final class NativeAdExecutor: NSObject, NativeAdLoaderDelegate, NativeAdDelegate {
    static let testAdUnitID = "ca-app-pub-3940256099942544/3986624511"
    private let maxAdsPerFeed = 3

    weak var plugin: AdMobPlugin?

    private final class State {
        let feedId: String
        let isSmall: Bool
        let sessionId: String
        let slotKey: String
        let nativeAd: NativeAd
        let adView: PluginNativeAdView
        let clippingView: UIView
        var generation = -1

        init(
            feedId: String,
            isSmall: Bool,
            sessionId: String,
            slotKey: String,
            nativeAd: NativeAd,
            adView: PluginNativeAdView,
            clippingView: UIView
        ) {
            self.feedId = feedId
            self.isSmall = isSmall
            self.sessionId = sessionId
            self.slotKey = slotKey
            self.nativeAd = nativeAd
            self.adView = adView
            self.clippingView = clippingView
        }
    }

    private struct Pending {
        let loader: AdLoader
        let call: CAPPluginCall
        let feedId: String
        let sessionId: String
        let slotKey: String
        let template: String
        let style: [String: Any]
    }

    private var states: [String: State] = [:]
    private var pending: [ObjectIdentifier: Pending] = [:]
    private let feedSessions = NativeAdFeedSessions()

    func startFeed(_ call: CAPPluginCall) {
        guard let session = NativeAdValues.session(call) else { return }
        let (feedId, sessionId) = session
        if !feedSessions.isCurrent(feedId: feedId, sessionId: sessionId) { clearFeed(feedId) }
        guard feedSessions.start(feedId: feedId, sessionId: sessionId) else {
            call.reject("At most two native ad feeds can be active")
            return
        }
        call.resolve()
    }

    func destroyFeed(_ call: CAPPluginCall) {
        guard let session = NativeAdValues.session(call) else { return }
        let (feedId, sessionId) = session
        if feedSessions.isCurrent(feedId: feedId, sessionId: sessionId) {
            clearFeed(feedId)
            feedSessions.remove(feedId: feedId)
        }
        call.resolve()
    }

    func load(_ call: CAPPluginCall, request: Request) {
        guard
            let feedId = NativeAdValues.requiredString(call, "feedId"),
            let sessionId = NativeAdValues.requiredString(call, "sessionId"),
            let slotKey = NativeAdValues.requiredString(call, "slotKey")
        else { return }
        guard feedSessions.isCurrent(feedId: feedId, sessionId: sessionId) else {
            call.reject("Native ad feed session is no longer active")
            return
        }
        let loader = AdLoader(
            adUnitID: Self.testAdUnitID,
            rootViewController: plugin?.getRootVC(),
            adTypes: [.native],
            options: nil
        )
        let identifier = ObjectIdentifier(loader)
        destroyState(NativeAdValues.key(feedId, slotKey))
        cancelPending(feedId: feedId, slotKey: slotKey)
        let feedAdCount = states.values.filter { $0.feedId == feedId }.count
            + pending.values.filter { $0.feedId == feedId }.count
        guard feedAdCount < maxAdsPerFeed else {
            call.reject("At most three native ads can be active in a feed")
            return
        }
        pending[identifier] = Pending(
            loader: loader,
            call: call,
            feedId: feedId,
            sessionId: sessionId,
            slotKey: slotKey,
            template: call.getString("template") ?? "medium",
            style: call.getObject("style") ?? [:]
        )
        loader.delegate = self
        loader.load(request)
    }

    func updatePlacements(_ call: CAPPluginCall) {
        guard
            let feedId = NativeAdValues.requiredString(call, "feedId"),
            let sessionId = NativeAdValues.requiredString(call, "sessionId")
        else { return }
        guard feedSessions.isCurrent(feedId: feedId, sessionId: sessionId) else {
            call.resolve()
            return
        }
        let sequence = call.getInt("sequence") ?? -1
        guard feedSessions.accepts(feedId: feedId, sequence: sequence) else {
            call.resolve()
            return
        }
        states.values.filter { $0.feedId == feedId }.forEach { $0.clippingView.isHidden = true }

        for case let placement as [String: Any] in call.getArray("placements") ?? [] {
            applyPlacement(placement, expectedFeedId: feedId)
        }
        call.resolve()
    }

    func remove(_ call: CAPPluginCall) {
        guard
            let feedId = NativeAdValues.requiredString(call, "feedId"),
            let sessionId = NativeAdValues.requiredString(call, "sessionId"),
            let slotKey = NativeAdValues.requiredString(call, "slotKey")
        else { return }
        guard feedSessions.isCurrent(feedId: feedId, sessionId: sessionId) else {
            call.resolve()
            return
        }
        cancelPending(feedId: feedId, slotKey: slotKey)
        destroyState(NativeAdValues.key(feedId, slotKey))
        call.resolve()
    }

    func destroyAll() {
        for request in pending.values { request.call.reject("Native ad load was cancelled") }
        for stateKey in Array(states.keys) { destroyState(stateKey) }
        pending.removeAll()
        feedSessions.removeAll()
    }

    func adLoader(_ adLoader: AdLoader, didReceive nativeAd: NativeAd) {
        let identifier = ObjectIdentifier(adLoader)
        guard let request = pending.removeValue(forKey: identifier) else { return }

        nativeAd.delegate = self
        let adView = PluginNativeAdView(nativeAd: nativeAd, template: request.template, style: request.style)
        let clippingView = UIView(frame: .zero)
        clippingView.clipsToBounds = true
        clippingView.isHidden = true
        clippingView.addSubview(adView)

        guard let webView = plugin?.bridge?.webView, let parent = webView.superview else {
            nativeAd.delegate = nil
            request.call.reject("AdMob WebView is unavailable")
            return
        }
        parent.addSubview(clippingView)
        let state = State(
            feedId: request.feedId,
            isSmall: request.template == "small",
            sessionId: request.sessionId,
            slotKey: request.slotKey,
            nativeAd: nativeAd,
            adView: adView,
            clippingView: clippingView
        )
        states[NativeAdValues.key(request.feedId, request.slotKey)] = state

        let feedId = request.feedId
        let sessionId = request.sessionId
        let slotKey = request.slotKey
        let adUnitID = adLoader.adUnitID
        nativeAd.paidEventHandler = { [weak self, weak nativeAd, weak state] adValue in
            guard let self, let nativeAd, let state,
                  self.states[NativeAdValues.key(feedId, slotKey)] === state else { return }
            self.plugin?.notifyListeners(NativeAdPluginEvents.adPaid.rawValue, data: [
                "feedId": feedId,
                "sessionId": sessionId,
                "slotKey": slotKey,
                "adUnitId": adUnitID,
                "valueMicros": adValue.value.int64Value,
                "currencyCode": adValue.currencyCode,
                "precision": adValue.precision.rawValue,
                "networkName": nativeAd.responseInfo.loadedAdNetworkResponseInfo?.adNetworkClassName ?? "",
                "impressionId": nativeAd.responseInfo.responseIdentifier ?? ""
            ])
        }

        plugin?.notifyListeners(
            NativeAdPluginEvents.loaded.rawValue,
            data: NativeAdValues.identity(request.feedId, request.sessionId, request.slotKey)
        )
        request.call.resolve()
    }

    private func applyPlacement(_ placement: [String: Any], expectedFeedId: String) {
        guard
            let placement = NativeAdPlacementValue(placement, expectedFeedId: expectedFeedId),
            let state = states[NativeAdValues.key(placement.feedId, placement.slotKey)],
            placement.generation >= state.generation,
            placement.rect.width >= (state.isSmall ? 120 : 144),
            placement.rect.height >= (state.isSmall ? 120 : 300),
            let webView = plugin?.bridge?.webView
        else { return }

        let webOrigin = webView.frame.origin
        state.clippingView.frame = placement.clip.offsetBy(dx: webOrigin.x, dy: webOrigin.y)
        state.adView.frame = CGRect(
            x: placement.rect.minX - placement.clip.minX,
            y: placement.rect.minY - placement.clip.minY,
            width: placement.rect.width,
            height: placement.rect.height
        )
        state.generation = placement.generation
        state.clippingView.isHidden = false
        state.clippingView.superview?.bringSubviewToFront(state.clippingView)
    }

    private func notify(_ event: NativeAdPluginEvents, nativeAd: NativeAd) {
        guard let state = states.values.first(where: { $0.nativeAd === nativeAd }) else { return }
        plugin?.notifyListeners(
            event.rawValue,
            data: NativeAdValues.identity(state.feedId, state.sessionId, state.slotKey)
        )
    }

    private func destroyState(_ stateKey: String) {
        guard let state = states.removeValue(forKey: stateKey) else { return }
        state.nativeAd.delegate = nil
        state.nativeAd.paidEventHandler = nil
        state.adView.nativeAd = nil
        state.clippingView.removeFromSuperview()
    }

    private func cancelPending(feedId: String, slotKey: String) {
        let identifiers = pending.compactMap { identifier, request in
            request.feedId == feedId && request.slotKey == slotKey ? identifier : nil
        }
        for identifier in identifiers {
            pending.removeValue(forKey: identifier)?.call.reject("Native ad load was cancelled")
        }
    }

    private func clearFeed(_ feedId: String) {
        let requests = pending.filter { $0.value.feedId == feedId }
        for (identifier, request) in requests {
            pending.removeValue(forKey: identifier)
            request.call.reject("Native ad load was cancelled")
        }
        for stateKey in states.filter({ $0.value.feedId == feedId }).keys {
            destroyState(stateKey)
        }
    }

}

extension NativeAdExecutor {
    func adLoader(_ adLoader: AdLoader, didFailToReceiveAdWithError error: Error) {
        let identifier = ObjectIdentifier(adLoader)
        guard let request = pending.removeValue(forKey: identifier) else { return }
        plugin?.notifyListeners(NativeAdPluginEvents.failedToLoad.rawValue, data: [
            "feedId": request.feedId,
            "sessionId": request.sessionId,
            "slotKey": request.slotKey,
            "code": (error as NSError).code,
            "message": error.localizedDescription
        ])
        request.call.reject(error.localizedDescription, String((error as NSError).code), error)
    }

    func nativeAdDidRecordClick(_ nativeAd: NativeAd) {
        notify(.clicked, nativeAd: nativeAd)
    }

    func nativeAdDidRecordImpression(_ nativeAd: NativeAd) {
        notify(.impression, nativeAd: nativeAd)
    }

    func nativeAdWillPresentScreen(_ nativeAd: NativeAd) {
        notify(.opened, nativeAd: nativeAd)
    }

    func nativeAdDidDismissScreen(_ nativeAd: NativeAd) {
        notify(.closed, nativeAd: nativeAd)
    }
}
