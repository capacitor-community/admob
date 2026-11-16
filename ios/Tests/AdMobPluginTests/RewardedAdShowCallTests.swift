import XCTest
@testable import AdMobPlugin

final class RewardedAdShowCallTests: XCTestCase {
    func testCancellationSettlesOnceAndIgnoresLateReward() {
        var rejections: [String] = []
        let show = RewardedAdShowCall(ad: NSObject(), adId: "test", resolve: { _ in
            XCTFail("Cancelled ad cannot award a reward")
        }, reject: { _, code in rejections.append(code) })
        XCTAssertTrue(show.finish())
        XCTAssertFalse(show.finish())
        XCTAssertFalse(show.reward(["amount": 1]))
        XCTAssertEqual(rejections, ["REWARDED_AD_CANCELLED"])
    }

    func testRewardAndDismissalResolveOnlyOnce() {
        var rewards = 0
        let show = RewardedAdShowCall(ad: NSObject(), adId: "test", resolve: { item in
            XCTAssertEqual(item["amount"] as? Int, 7)
            rewards += 1
        }, reject: { _, _ in XCTFail("Earned reward cannot be cancelled") })
        XCTAssertTrue(show.reward(["amount": 7]))
        XCTAssertFalse(show.reward(["amount": 7]))
        XCTAssertTrue(show.finish())
        XCTAssertFalse(show.finish())
        XCTAssertEqual(rewards, 1)
    }

    func testPresentationFailurePreservesSdkCode() {
        var rejections: [String] = []
        let show = RewardedAdShowCall(ad: NSObject(), adId: "test", resolve: { _ in
            XCTFail("Failed ad cannot resolve")
        }, reject: { message, code in
            XCTAssertEqual(message, "Native failure")
            rejections.append(code)
        })
        XCTAssertTrue(show.finish(error: NSError(domain: "ads", code: 9,
                                                userInfo: [NSLocalizedDescriptionKey: "Native failure"])))
        XCTAssertFalse(show.finish())
        XCTAssertEqual(rejections, ["9"])
    }

    func testSettledCallReleasesBothClosures() {
        weak var retained: NSObject?
        let show: RewardedAdShowCall
        do {
            let owner = NSObject()
            retained = owner
            show = RewardedAdShowCall(ad: NSObject(), adId: "test",
                                      resolve: { _ in _ = owner }, reject: { _, _ in _ = owner })
        }
        XCTAssertNotNil(retained)
        show.finish()
        XCTAssertNil(retained)
    }
}
