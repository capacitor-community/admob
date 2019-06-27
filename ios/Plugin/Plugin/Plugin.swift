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
    var bannerUIView: UIViewController!

    @objc func initialize(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? "ca-app-pub-6564742920318187~7217030993"
        call.success([
            "value": appId
            ])
    }

    @objc func showBanner(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.bannerView = GADBannerView(adSize: kGADAdSizeBanner)
            self.bannerView.translatesAutoresizingMaskIntoConstraints = false
            self.bannerView.adUnitID = "ca-app-pub-3940256099942544/2934735716"
            self.bannerView.backgroundColor = UIColor(red: 10, green: 0, blue: 0, alpha: 100)

            if let rootViewController = UIApplication.shared.keyWindow?.rootViewController {
                rootViewController.view.addSubview(self.bannerView)
                rootViewController.view.addConstraints(
                    [NSLayoutConstraint(item: self.bannerView,
                                        attribute: .bottom,
                                        relatedBy: .equal,
                                        toItem: self.bannerView,
                                        attribute: .top,
                                        multiplier: 1,
                                        constant: 0),
                     NSLayoutConstraint(item: self.bannerView,
                                        attribute: .centerX,
                                        relatedBy: .equal,
                                        toItem: rootViewController.view,
                                        attribute: .centerX,
                                        multiplier: 1,
                                        constant: 0)
                    ])
                self.bannerView.load(GADRequest())
            }
        }
    }
}
