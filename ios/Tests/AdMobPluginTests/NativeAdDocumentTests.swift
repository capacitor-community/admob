import Capacitor
import WebKit
import XCTest
@testable import AdMobPlugin

@MainActor
final class NativeAdDocumentTests: XCTestCase, WKNavigationDelegate {
    private var loaded: XCTestExpectation?

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation?) {
        loaded?.fulfill()
        loaded = nil
    }

    func testReplacementDocumentGetsItsOwnFeedCapacity() async throws {
        let webView = WKWebView()
        webView.navigationDelegate = self
        let plugin = AdMobPlugin()
        plugin.webView = webView

        func navigate() async {
            let finished = expectation(description: "Main document loaded")
            loaded = finished
            webView.loadHTMLString("<body>Feed</body>", baseURL: nil)
            await fulfillment(of: [finished], timeout: 10)
        }

        func start(_ id: String, expectsSuccess: Bool) async throws {
            let settled = expectation(description: "Start feed " + id)
            let call = try XCTUnwrap(CAPPluginCall(
                callbackId: id, methodName: "startNativeAdFeed", options: ["feedId": id, "sessionId": id],
                success: { _, _ in
                    XCTAssertTrue(expectsSuccess)
                    settled.fulfill()
                }, error: { _ in
                    XCTAssertFalse(expectsSuccess)
                    settled.fulfill()
                }))
            plugin.startNativeAdFeed(call)
            await fulfillment(of: [settled], timeout: 10)
        }

        await navigate()
        try await start("first", expectsSuccess: true)
        try await start("second", expectsSuccess: true)
        try await start("third", expectsSuccess: false)
        await navigate()
        try await start("third", expectsSuccess: true)
    }
}
