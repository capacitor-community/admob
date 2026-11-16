import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class AdaptiveBannerSizeTests: XCTestCase {
    @MainActor
    func testExplicitWidthUsesSdkWithoutCreatingABanner() {
        let executor = BannerExecutor()
        let expected = currentOrientationAnchoredAdaptiveBanner(width: 300).size
        let call = CAPPluginCall(callbackId: "adaptive", methodName: "getAdaptiveBannerSize", options: ["width": 300], success: { result, _ in
            XCTAssertEqual((result?.data?["width"] as? NSNumber)?.doubleValue, Double(expected.width))
            XCTAssertEqual((result?.data?["height"] as? NSNumber)?.doubleValue, Double(expected.height))
        }, error: { _ in XCTFail("Adaptive size should resolve") })!
        executor.getAdaptiveBannerSize(call)
        XCTAssertNil(executor.bannerView)
    }

    @MainActor
    func testInvalidWidthsRejectWithoutCreatingABanner() {
        for width: Any in [0, -1, Double.infinity, Double.nan, Double(Int32.max) + 1, "invalid", NSNull()] {
            let executor = BannerExecutor()
            var rejected = false
            let call = CAPPluginCall(callbackId: "adaptive", methodName: "getAdaptiveBannerSize", options: ["width": width], success: { _, _ in
                XCTFail("Invalid width should reject")
            }, error: { _ in rejected = true })!
            executor.getAdaptiveBannerSize(call)
            XCTAssertTrue(rejected)
            XCTAssertNil(executor.bannerView)
        }
    }
}
