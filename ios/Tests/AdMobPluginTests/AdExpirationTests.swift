import XCTest
@testable import AdMobPlugin

final class AdExpirationTests: XCTestCase {
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
