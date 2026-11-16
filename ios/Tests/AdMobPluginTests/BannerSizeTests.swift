import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class BannerSizeTests: XCTestCase {
    @MainActor
    func testReturnsOnlyTheDisplayedBannerSize() {
        let executor = BannerExecutor()
        func check(_ width: CGFloat, _ height: CGFloat) {
            let call = CAPPluginCall(callbackId: "size", methodName: "getBannerSize", options: [:], success: { result, _ in
                XCTAssertEqual((result?.data?["width"] as? NSNumber)?.doubleValue, Double(width))
                XCTAssertEqual((result?.data?["height"] as? NSNumber)?.doubleValue, Double(height))
            }, error: { _ in XCTFail("Size query should resolve") })!
            executor.getBannerSize(call)
        }
        check(0, 0)
        let banner = BannerView(adSize: AdSizeBanner)
        banner.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
        executor.bannerView = banner
        check(0, 0)
        let parent = UIView()
        parent.addSubview(banner)
        check(320, 50)
        banner.isHidden = true
        check(0, 0)
        banner.isHidden = false
        check(320, 50)
        banner.removeFromSuperview()
        check(0, 0)
    }
}
