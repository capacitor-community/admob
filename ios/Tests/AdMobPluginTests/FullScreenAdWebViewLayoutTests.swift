import XCTest
import UIKit
@testable import AdMobPlugin

final class FullScreenAdWebViewLayoutTests: XCTestCase {
    @MainActor
    func testRestoresNonOverlayingStatusBarFrame() {
        let parent = UIView(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
        let webView = UIView(frame: CGRect(x: 0, y: 54, width: 402, height: 820))
        parent.addSubview(webView)
        let layout = FullScreenAdWebViewLayout(webView: webView)
        webView.frame = parent.bounds
        layout?.restore()
        XCTAssertEqual(webView.frame, CGRect(x: 0, y: 54, width: 402, height: 820))
    }

    @MainActor
    func testDoesNotApplyStalePortraitFrameAfterRotation() {
        let parent = UIView(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
        let webView = UIView(frame: CGRect(x: 0, y: 54, width: 402, height: 820))
        parent.addSubview(webView)
        let layout = FullScreenAdWebViewLayout(webView: webView)
        parent.bounds.size = CGSize(width: 874, height: 402)
        webView.frame = parent.bounds
        layout?.restore()
        XCTAssertEqual(webView.frame, parent.bounds)
    }

    @MainActor
    func testDoesNotChangeAReparentedWebView() {
        let original = UIView(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
        let replacement = UIView(frame: original.frame)
        let webView = UIView(frame: CGRect(x: 0, y: 54, width: 402, height: 820))
        original.addSubview(webView)
        let layout = FullScreenAdWebViewLayout(webView: webView)
        replacement.addSubview(webView)
        webView.frame = replacement.bounds
        layout?.restore()
        XCTAssertEqual(webView.frame, replacement.bounds)
    }
}
