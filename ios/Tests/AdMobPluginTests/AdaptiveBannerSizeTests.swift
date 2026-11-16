import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class AdaptiveBannerSizeTests: XCTestCase {
    @MainActor
    func testExplicitWidthMatchesSdkWithoutCreatingABanner() throws {
        let executor = BannerExecutor()
        let expected = currentOrientationAnchoredAdaptiveBanner(width: 300).size
        var settled = false
        let call = try XCTUnwrap(CAPPluginCall(
            callbackId: "adaptive", methodName: "getAdaptiveBannerSize", options: ["width": 300], success: { result, _ in
            settled = true
            XCTAssertGreaterThan(expected.width, 0)
            XCTAssertGreaterThan(expected.height, 0)
            XCTAssertEqual((result?.data?["width"] as? NSNumber)?.doubleValue, Double(expected.width))
            XCTAssertEqual((result?.data?["height"] as? NSNumber)?.doubleValue, Double(expected.height))
        }, error: { _ in XCTFail("Adaptive size should resolve") }))
        executor.getAdaptiveBannerSize(call)
        XCTAssertTrue(settled)
        XCTAssertNil(executor.bannerView)
    }

    @MainActor
    func testDefaultSizeIsAvailableBeforeSdkInitialization() async throws {
        let plugin = AdaptiveSizeTestPlugin()
        plugin.load()
        let expected = currentOrientationAnchoredAdaptiveBanner(width: 320).size
        let resolved = expectation(description: "Size before initialize")
        let call = try XCTUnwrap(CAPPluginCall(
            callbackId: "adaptive", methodName: "getAdaptiveBannerSize", options: [:], success: { result, _ in
            XCTAssertEqual((result?.data?["width"] as? NSNumber)?.doubleValue, Double(expected.width))
            XCTAssertEqual((result?.data?["height"] as? NSNumber)?.doubleValue, Double(expected.height))
            resolved.fulfill()
        }, error: { _ in XCTFail("Default size should resolve before initialize") }))
        plugin.getAdaptiveBannerSize(call)
        await fulfillment(of: [resolved], timeout: 2)
    }

    @MainActor
    func testInvalidWidthsRejectWithoutCreatingABanner() throws {
        for width: Any in [0, -1, Double.infinity, Double.nan, Double(Int32.max) + 1, "invalid", NSNull()] {
            let executor = BannerExecutor()
            var rejected = false
            let call = try XCTUnwrap(CAPPluginCall(
                callbackId: "adaptive", methodName: "getAdaptiveBannerSize", options: ["width": width], success: { _, _ in
                XCTFail("Invalid width should reject")
            }, error: { _ in rejected = true }))
            executor.getAdaptiveBannerSize(call)
            XCTAssertTrue(rejected)
            XCTAssertNil(executor.bannerView)
        }
    }
}

private final class AdaptiveSizeTestPlugin: AdMobPlugin {
    private let controller = UIViewController()
    override func getRootVC() -> UIViewController? {
        controller.view.frame = CGRect(x: 0, y: 0, width: 320, height: 640)
        return controller
    }
}
