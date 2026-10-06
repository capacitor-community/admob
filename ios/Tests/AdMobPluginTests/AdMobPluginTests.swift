import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class AdMobPluginTests: XCTestCase {
    func testVolumeIsClampedToSupportedRange() {
        let originalVolume = MobileAds.shared.applicationVolume
        defer { MobileAds.shared.applicationVolume = originalVolume }
        let plugin = AdMobPlugin()

        for (input, expected) in [(-0.5, 0.0), (0.4, 0.4), (1.5, 1.0)] {
            assertResolves(method: "setApplicationVolume", options: ["volume": input]) {
                plugin.setApplicationVolume($0)
            }
            XCTAssertEqual(Double(MobileAds.shared.applicationVolume), expected, accuracy: 0.0001)
        }
    }

    func testMissingVolumeRejectsWithoutChangingVolume() {
        let originalVolume = MobileAds.shared.applicationVolume
        defer { MobileAds.shared.applicationVolume = originalVolume }
        MobileAds.shared.applicationVolume = 0.4

        assertRejects(method: "setApplicationVolume") { AdMobPlugin().setApplicationVolume($0) }

        XCTAssertEqual(Double(MobileAds.shared.applicationVolume), 0.4, accuracy: 0.0001)
    }

    func testMuteCanBeEnabledAndDisabled() {
        let originalMuted = MobileAds.shared.isApplicationMuted
        defer { MobileAds.shared.isApplicationMuted = originalMuted }
        let plugin = AdMobPlugin()

        for muted in [true, false] {
            assertResolves(method: "setApplicationMuted", options: ["muted": muted]) {
                plugin.setApplicationMuted($0)
            }
            XCTAssertEqual(MobileAds.shared.isApplicationMuted, muted)
        }
    }

    func testMissingMutedRejectsWithoutChangingMuteState() {
        let originalMuted = MobileAds.shared.isApplicationMuted
        defer { MobileAds.shared.isApplicationMuted = originalMuted }
        MobileAds.shared.isApplicationMuted = true

        assertRejects(method: "setApplicationMuted") { AdMobPlugin().setApplicationMuted($0) }

        XCTAssertTrue(MobileAds.shared.isApplicationMuted)
    }

    func testInterstitialWithoutPreparedAdRejects() {
        assertRejects(method: "showInterstitial") { AdInterstitialExecutor().showInterstitial($0) }
    }

    func testRewardedWithoutPreparedAdRejects() {
        assertRejects(method: "showRewardVideoAd") { AdRewardExecutor().showRewardVideoAd($0) }
    }

    func testRewardedInterstitialWithoutPreparedAdRejects() {
        assertRejects(method: "showRewardInterstitialAd") { AdRewardInterstitialExecutor().showRewardInterstitialAd($0) }
    }

    private func assertResolves(method: String, options: [String: Any], operation: (CAPPluginCall) -> Void) {
        let resolved = expectation(description: "\(method) resolves")
        let call = CAPPluginCall(callbackId: "test", methodName: method, options: options, success: { _, _ in
            resolved.fulfill()
        }, error: { error in
            XCTFail("Unexpected rejection: \(error)")
        })!
        operation(call)
        wait(for: [resolved], timeout: 1)
    }

    private func assertRejects(method: String, operation: (CAPPluginCall) -> Void) {
        let rejected = expectation(description: "\(method) rejects")
        let call = CAPPluginCall(callbackId: "test", methodName: method, options: [:], success: { _, _ in
            XCTFail("Expected rejection from \(method)")
        }, error: { _ in
            rejected.fulfill()
        })!
        operation(call)
        wait(for: [rejected], timeout: 1)
    }
}
