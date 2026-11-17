import XCTest
@testable import AdMobPlugin

final class AdExpirationTests: XCTestCase {
    func testPrivacyManifestIsBundledWithElapsedTimeReason() throws {
        let url = try XCTUnwrap(Bundle.module.url(forResource: "PrivacyInfo", withExtension: "xcprivacy"))
        let data = try Data(contentsOf: url)
        let manifest = try XCTUnwrap(PropertyListSerialization.propertyList(from: data, format: nil) as? [String: Any])
        let entries = try XCTUnwrap(manifest["NSPrivacyAccessedAPITypes"] as? [[String: Any]])
        let reasons = entries.first { $0["NSPrivacyAccessedAPIType"] as? String == "NSPrivacyAccessedAPICategorySystemBootTime" }
        XCTAssertEqual(reasons?["NSPrivacyAccessedAPITypeReasons"] as? [String], ["35F9.1"])
    }

    func testExpirationBoundaryAndReload() {
        for maxAge: TimeInterval in [3600, 14400] {
            var now: TimeInterval = 10
            let expiration = AdExpiration(maxAge: maxAge, clock: { now })
            now += maxAge - 0.001
            XCTAssertFalse(expiration.isExpired)
            now += 0.001
            XCTAssertTrue(expiration.isExpired)
            let reloaded = AdExpiration(maxAge: maxAge, clock: { now })
            XCTAssertFalse(reloaded.isExpired)
            now = 0
            XCTAssertTrue(reloaded.isExpired)
        }
    }
}
