import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class BannerSizeTests: XCTestCase {
    @MainActor
    func testReturnsOnlyTheDisplayedBannerSize() throws {
        let executor = BannerExecutor()
        func check(_ width: CGFloat, _ height: CGFloat) throws {
            let call = try XCTUnwrap(CAPPluginCall(
                callbackId: "size", methodName: "getBannerSize", options: [:], success: { result, _ in
                XCTAssertEqual((result?.data?["width"] as? NSNumber)?.doubleValue, Double(width))
                XCTAssertEqual((result?.data?["height"] as? NSNumber)?.doubleValue, Double(height))
            }, error: { _ in XCTFail("Size query should resolve") }))
            executor.getBannerSize(call)
        }
        try check(0, 0)
        let banner = BannerView(adSize: AdSizeBanner)
        banner.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
        executor.bannerView = banner
        try check(0, 0)
        let parent = UIView()
        parent.addSubview(banner)
        try check(320, 50)
        banner.isHidden = true
        try check(0, 0)
        banner.isHidden = false
        try check(320, 50)
        banner.removeFromSuperview()
        try check(0, 0)
    }
}
