import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class BannerDisplayTests: XCTestCase {
    @MainActor
    func testReplacedBannerCannotChangeTheCurrentBanner() throws {
        let plugin = BannerDisplayTestPlugin()
        let root = try XCTUnwrap(plugin.getRootVC()?.view)
        let executor = BannerExecutor()
        executor.plugin = plugin
        executor.showBanner(try showBannerCall(), Request(), testAdUnitID)
        let replaced = try XCTUnwrap(executor.bannerView)
        executor.showBanner(try showBannerCall(), Request(), testAdUnitID)
        let current = try XCTUnwrap(executor.bannerView)
        executor.bannerViewDidReceiveAd(current)

        // Late results of the replaced banner's request, as the SDK would deliver them.
        replaced.delegate?.bannerViewDidReceiveAd?(replaced)
        XCTAssertNil(replaced.superview)
        XCTAssertTrue(current.superview === root)

        replaced.delegate?.bannerView?(replaced, didFailToReceiveAdWithError: NSError(domain: "test", code: 1))
        XCTAssertTrue(current.superview === root)
        XCTAssertTrue(current.delegate === executor)
    }

    @MainActor
    func testRefreshKeepsBannerInFrontWithoutAddingConstraints() throws {
        let plugin = BannerDisplayTestPlugin()
        let root = try XCTUnwrap(plugin.getRootVC()?.view)
        let executor = BannerExecutor()
        executor.plugin = plugin
        let banner = BannerView(adSize: AdSizeBanner)
        banner.translatesAutoresizingMaskIntoConstraints = false

        executor.bannerViewDidReceiveAd(banner)
        root.addSubview(UIView())
        executor.bannerViewDidReceiveAd(banner)

        XCTAssertTrue(root.subviews.last === banner)
        XCTAssertEqual(root.constraints.filter { $0.firstItem === banner }.count, 2)
    }

    private let testAdUnitID = "ca-app-pub-3940256099942544/2934735716"

    private func showBannerCall() throws -> CAPPluginCall {
        try XCTUnwrap(CAPPluginCall(
            callbackId: "show", methodName: "showBanner", options: ["adSize": "BANNER"],
            success: { _, _ in }, error: { _ in XCTFail("showBanner should resolve") }))
    }
}

private final class BannerDisplayTestPlugin: AdMobPlugin {
    private let controller = UIViewController()
    override func getRootVC() -> UIViewController? {
        controller.view.frame = CGRect(x: 0, y: 0, width: 320, height: 640)
        return controller
    }
}
