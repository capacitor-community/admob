import XCTest
import WebKit
@testable import AdMobPlugin

final class NativeAdScrollTrackerTests: XCTestCase {
    @MainActor
    func testScrollClipsPlacementsAndReleasingTrackerStopsUpdates() throws {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 320, height: 640))
        let web = WKWebView(frame: window.bounds)
        window.addSubview(web)
        let scroll = UIScrollView(frame: CGRect(x: 0, y: 80, width: 320, height: 500))
        scroll.contentSize = CGSize(width: 320, height: 1200)
        web.addSubview(scroll)
        let placement = try XCTUnwrap(NativeAdPlacementValue([
            "feedId": "feed", "slotKey": "ad", "generation": 1, "visible": true,
            "rect": ["x": 10, "y": 400, "width": 300, "height": 320],
            "clipRect": ["x": 10, "y": 400, "width": 300, "height": 320]
        ], expectedFeedId: "feed"))
        let container: [String: Any] = [
            "rect": ["x": 0, "y": 80, "width": 320, "height": 500],
            "clipRect": ["x": 0, "y": 100, "width": 320, "height": 480],
            "contentWidth": 320.0, "contentHeight": 1200.0
        ]
        var rendered: [NativeAdPlacementValue] = []
        var updates = 0
        var tracker: NativeAdScrollTracker? = try XCTUnwrap(NativeAdScrollTracker(
            container: container, webView: web, placements: [placement],
            render: { rendered = $0; updates += 1 }
        ))
        tracker?.update()
        XCTAssertEqual(rendered.first?.clip, CGRect(x: 10, y: 480, width: 300, height: 100))
        scroll.contentOffset.y = 450
        XCTAssertEqual(rendered.first?.rect, CGRect(x: 10, y: 30, width: 300, height: 320))
        XCTAssertEqual(rendered.first?.clip, CGRect(x: 10, y: 100, width: 300, height: 250))
        scroll.contentOffset.y = 800
        XCTAssertTrue(rendered.isEmpty)
        tracker = nil
        let previousUpdates = updates
        scroll.contentOffset.y = 0
        XCTAssertEqual(updates, previousUpdates)

        let duplicate = UIScrollView(frame: scroll.frame)
        duplicate.contentSize = scroll.contentSize
        web.addSubview(duplicate)
        XCTAssertNil(NativeAdScrollTracker(container: container, webView: web, placements: [placement], render: { _ in }))
    }
}
