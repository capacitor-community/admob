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
}

private final class MediaTestPlugin: AdMobPlugin {
    var onSuspension: ((Bool) -> Void)?

    @MainActor
    override func setWebMediaPlaybackSuspended(_ suspended: Bool) {
        onSuspension?(suspended)
    }
}
