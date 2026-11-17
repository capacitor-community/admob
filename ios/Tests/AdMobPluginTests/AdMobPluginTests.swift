import XCTest
@testable import AdMobPlugin

final class AdMobPluginTests: XCTestCase {
    @MainActor
    func testNormalSuspensionAndResume() async {
        await checkSuspensions([(0, true, true), (0, false, false)])
    }

    @MainActor
    func testFailedAdCannotReleaseAnotherAdsSuspension() async {
        await checkSuspensions([(0, true, true), (1, false, nil), (0, false, false)])
    }

    @MainActor
    func testOnlyLastAdResumesMedia() async {
        await checkSuspensions([(0, true, true), (1, true, nil), (0, false, nil), (1, false, false)])
    }

    @MainActor
    private func checkSuspensions(_ steps: [(Int, Bool, Bool?)]) async {
        let plugin = MediaTestPlugin()
        let owners = [NSObject(), NSObject()]

        for (owner, suspend, expected) in steps {
            let called = expectation(description: "WebKit suspension: \(String(describing: expected))")
            called.isInverted = expected == nil
            plugin.onSuspension = { actual in
                XCTAssertEqual(actual, expected)
                called.fulfill()
            }
            if suspend {
                plugin.suspendWebMedia(for: owners[owner])
            } else {
                plugin.resumeWebMedia(for: owners[owner])
            }
            await fulfillment(of: [called], timeout: expected == nil ? 0.1 : 2)
        }
        plugin.onSuspension = nil
    }

    func testNativeFeedSessionsEnforceCapacityAndAllowReplacement() {
        let sessions = NativeAdFeedSessions()

        XCTAssertTrue(sessions.start(feedId: "first", sessionId: "session-1"))
        XCTAssertTrue(sessions.start(feedId: "second", sessionId: "session-2"))
        XCTAssertFalse(sessions.start(feedId: "third", sessionId: "session-3"))
        XCTAssertTrue(sessions.start(feedId: "first", sessionId: "replacement"))
        XCTAssertTrue(sessions.isCurrent(feedId: "first", sessionId: "replacement"))
    }

    func testNativeFeedSessionsRejectStalePlacementSequences() {
        let sessions = NativeAdFeedSessions()
        XCTAssertTrue(sessions.start(feedId: "feed", sessionId: "session"))

        XCTAssertTrue(sessions.accepts(feedId: "feed", sequence: 1))
        XCTAssertFalse(sessions.accepts(feedId: "feed", sequence: 1))
        XCTAssertFalse(sessions.accepts(feedId: "feed", sequence: 0))
        XCTAssertTrue(sessions.accepts(feedId: "feed", sequence: 2))
    }

    func testNativePlacementRejectsWrongFeedAndMalformedRect() {
        let valid: [String: Any] = [
            "visible": true,
            "feedId": "feed",
            "slotKey": "slot",
            "generation": 1,
            "rect": ["x": 0, "y": 0, "width": 144, "height": 300],
            "clipRect": ["x": 0, "y": 0, "width": 144, "height": 300]
        ]

        XCTAssertNotNil(NativeAdPlacementValue(valid, expectedFeedId: "feed"))
        XCTAssertNil(NativeAdPlacementValue(valid, expectedFeedId: "another-feed"))
        var malformed = valid
        malformed["rect"] = ["x": 0, "y": 0, "width": 0, "height": 300]
        XCTAssertNil(NativeAdPlacementValue(malformed, expectedFeedId: "feed"))
    }
}

private final class MediaTestPlugin: AdMobPlugin {
    var onSuspension: ((Bool) -> Void)?

    @MainActor
    override func setWebMediaPlaybackSuspended(_ suspended: Bool) {
        onSuspension?(suspended)
    }
}
