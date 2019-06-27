import Foundation
import Capacitor
import GoogleMobileAds

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(AdMob)
public class AdMob: CAPPlugin {

    var bannerView: GADBannerView!

    @objc func initialize(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? "ca-app-pub-6564742920318187~7217030993"
        call.success([
            "value": appId
            ])
    }

    @objc func showBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.bannerView = GADBannerView(adSize: kGADAdSizeBanner)

            self.addBannerViewToView(self.bannerView)
            self.bannerView.translatesAutoresizingMaskIntoConstraints = false
            self.bannerView.adUnitID = "ca-app-pub-3940256099942544/2934735716"
            self.bannerView.rootViewController = UIApplication.shared.keyWindow?.rootViewController
            self.bannerView.load(GADRequest())
        }
    }

    func addBannerViewToView(_ bannerView: GADBannerView) {
        if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
            NSLog("AdMob: rendering rootView")
            bannerView.translatesAutoresizingMaskIntoConstraints = false
            rootViewController.view.addSubview(bannerView)
            rootViewController.view.addConstraints(
                [NSLayoutConstraint(item: bannerView,
                                    attribute: .bottom,
                                    relatedBy: .equal,
                                    toItem: rootViewController.bottomLayoutGuide,
                                    attribute: .top,
                                    multiplier: 1,
                                    constant: 0),
                 NSLayoutConstraint(item: bannerView,
                                    attribute: .centerX,
                                    relatedBy: .equal,
                                    toItem: rootViewController.view,
                                    attribute: .centerX,
                                    multiplier: 1,
                                    constant: 0)
                ])
        }
    }
}
