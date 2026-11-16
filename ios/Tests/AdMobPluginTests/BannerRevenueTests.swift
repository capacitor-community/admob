import XCTest
import Capacitor
import GoogleMobileAds
@testable import AdMobPlugin

final class BannerRevenueTests: XCTestCase {
    @MainActor
    func testPaidCallbacksOnlyReportForTheCurrentBanner() throws {
        let plugin = BannerRevenueTestPlugin()
        let executor = BannerExecutor()
        executor.plugin = plugin
        let original = BannerView(adSize: AdSizeBanner)
        original.adUnitID = "original"
        executor.bannerView = original
        executor.setPaidEventHandler(original)
        let originalHandler = try XCTUnwrap(original.paidEventHandler)
        originalHandler(BannerTestAdValue())
        XCTAssertEqual(plugin.revenue.map { $0["adUnitId"] as? String }, ["original"])

        let remove = try XCTUnwrap(CAPPluginCall(callbackId: "remove", methodName: "removeBanner", options: [:],
            success: { _, _ in }, error: { _ in XCTFail("Removal should resolve") }))
        executor.removeBanner(remove)
        originalHandler(BannerTestAdValue())
        XCTAssertEqual(plugin.revenue.count, 1)

        let replacement = BannerView(adSize: AdSizeBanner)
        replacement.adUnitID = "replacement"
        executor.bannerView = replacement
        executor.setPaidEventHandler(replacement)
        originalHandler(BannerTestAdValue())
        XCTAssertEqual(plugin.revenue.count, 1)
        let replacementHandler = try XCTUnwrap(replacement.paidEventHandler)
        replacementHandler(BannerTestAdValue())
        XCTAssertEqual(plugin.revenue.map { $0["adUnitId"] as? String }, ["original", "replacement"])

        executor.bannerView(replacement, didFailToReceiveAdWithError: NSError(domain: "ads", code: 3))
        replacementHandler(BannerTestAdValue())
        XCTAssertEqual(plugin.revenue.count, 2)
    }
}

private final class BannerRevenueTestPlugin: AdMobPlugin {
    var revenue: [[String: Any]] = []
    override func notifyListeners(_ eventName: String, data: [String: Any]?) {
        if eventName == BannerAdPluginEvents.AdPaid.rawValue, let data = data { revenue.append(data) }
    }
}

private final class BannerTestAdValue: AdValue {
    override var value: NSDecimalNumber { NSDecimalNumber(string: "0.01") }
    override var currencyCode: String { "USD" }
    override var precision: AdValuePrecision { .precise }
}
