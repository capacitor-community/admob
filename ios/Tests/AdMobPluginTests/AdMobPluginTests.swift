import XCTest
@testable import AdMobPlugin

class AdMobTests: XCTestCase {
    func testEcho() {
        // This is an example of a functional test case for a plugin.
        // Use XCTAssert and related functions to verify your tests produce the correct results.

        //         let implementation = AdMob()
        //         let value = "Hello, World!"
        //         let result = implementation.echo(value)
        //
        //         XCTAssertEqual(value, result)
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
