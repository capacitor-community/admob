import Foundation
import Capacitor
import GoogleMobileAds
import UserMessagingPlatform

class ConsentExecutor: NSObject {
    weak var plugin: AdMobPlugin?

    func requestConsentInfo(_ call: CAPPluginCall, _ debugGeography: Int, _ testDeviceIdentifiers: [String], _ tagForUnderAgeOfConsent: Bool) {
        let parameters = UMPRequestParameters()
        let debugSettings = UMPDebugSettings()

        debugSettings.geography = UMPDebugGeography(rawValue: debugGeography) ?? UMPDebugGeography.disabled
        debugSettings.testDeviceIdentifiers = testDeviceIdentifiers

        parameters.debugSettings = debugSettings
        parameters.tagForUnderAgeOfConsent = tagForUnderAgeOfConsent

        // Request an update to the consent information.
        UMPConsentInformation.sharedInstance.requestConsentInfoUpdate(
            with: parameters,
            completionHandler: { error in
                if error != nil {
                    call.reject("Request consent info failed")
                } else {
                    call.resolve([
                        "status": self.getConsentStatusString(UMPConsentInformation.sharedInstance.consentStatus),
                        "isConsentFormAvailable": UMPConsentInformation.sharedInstance.formStatus == UMPFormStatus.available,
                        "canShowAds": self.canShowAds(),
                        "canShowPersonalizedAds": self.canShowPersonalizedAds(),
                        "isConsentOutdated": self.isConsentOutdated()
                    ])
                }
            })
    }

    func showConsentForm(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            let formStatus = UMPConsentInformation.sharedInstance.formStatus

            if formStatus == UMPFormStatus.available {
                UMPConsentForm.load(completionHandler: {form, loadError in
                    if loadError != nil {
                        call.reject(loadError?.localizedDescription ?? "Load consent form error")
                        return
                    }

                    form?.present(from: rootViewController, completionHandler: { dismissError in
                        if dismissError != nil {
                            call.reject(dismissError?.localizedDescription ?? "Consent dismiss error")
                            return
                        }

                        call.resolve([
                            "status": self.getConsentStatusString(UMPConsentInformation.sharedInstance.consentStatus),
                            "canShowAds": self.canShowAds(),
                            "canShowPersonalizedAds": self.canShowPersonalizedAds(),
                            "isConsentOutdated": self.isConsentOutdated()
                        ])
                    })
                })
            } else {
                call.reject("Consent Form not available")
            }
        } else {
            call.reject("No ViewController")
        }
    }

    func resetConsentInfo(_ call: CAPPluginCall) {
        UMPConsentInformation.sharedInstance.reset()
        call.resolve()
    }

    func getConsentStatusString(_ consentStatus: UMPConsentStatus) -> String {
        switch consentStatus {
        case UMPConsentStatus.required:
            return "REQUIRED"
        case UMPConsentStatus.notRequired:
            return "NOT_REQUIRED"
        case UMPConsentStatus.obtained:
            return "OBTAINED"
        default:
            return "UNKNOWN"
        }
    }

    func isGDPR() -> Bool {
        let settings = UserDefaults.standard
        let gdpr = settings.integer(forKey: "IABTCF_gdprApplies")
        return gdpr == 1
    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private func hasAttribute(input: String, index: Int) -> Bool {
        return input.count >= index && String(Array(input)[index-1]) == "1"
    }

    // Check if consent is given for a list of purposes
    private func hasConsentFor(_ purposes: [Int], _ purposeConsent: String, _ hasVendorConsent: Bool) -> Bool {
        return purposes.allSatisfy { i in hasAttribute(input: purposeConsent, index: i) } && hasVendorConsent
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private func hasConsentOrLegitimateInterestFor(_ purposes: [Int], _ purposeConsent: String, _ purposeLI: String, _ hasVendorConsent: Bool, _ hasVendorLI: Bool) -> Bool {
        return purposes.allSatisfy { i in
            (hasAttribute(input: purposeLI, index: i) && hasVendorLI) ||
            (hasAttribute(input: purposeConsent, index: i) && hasVendorConsent)
        }
    }

    private func canShowAds() -> Bool {
        let settings = UserDefaults.standard

        //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        let purposeConsent = settings.string(forKey: "IABTCF_PurposeConsents") ?? ""
        let vendorConsent = settings.string(forKey: "IABTCF_VendorConsents") ?? ""
        let vendorLI = settings.string(forKey: "IABTCF_VendorLegitimateInterests") ?? ""
        let purposeLI = settings.string(forKey: "IABTCF_PurposeLegitimateInterests") ?? ""

        let googleId = 755
        let hasGoogleVendorConsent = hasAttribute(input: vendorConsent, index: googleId)
        let hasGoogleVendorLI = hasAttribute(input: vendorLI, index: googleId)

        // Minimum required for at least non-personalized ads
        return hasConsentFor([1], purposeConsent, hasGoogleVendorConsent)
            && hasConsentOrLegitimateInterestFor([2,7,9,10], purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI)

    }

    private func canShowPersonalizedAds() -> Bool {
        let settings = UserDefaults.standard

        //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        // required for personalized ads
        let purposeConsent = settings.string(forKey: "IABTCF_PurposeConsents") ?? ""
        let vendorConsent = settings.string(forKey: "IABTCF_VendorConsents") ?? ""
        let vendorLI = settings.string(forKey: "IABTCF_VendorLegitimateInterests") ?? ""
        let purposeLI = settings.string(forKey: "IABTCF_PurposeLegitimateInterests") ?? ""

        let googleId = 755
        let hasGoogleVendorConsent = hasAttribute(input: vendorConsent, index: googleId)
        let hasGoogleVendorLI = hasAttribute(input: vendorLI, index: googleId)

        return hasConsentFor([1,3,4], purposeConsent, hasGoogleVendorConsent)
            && hasConsentOrLegitimateInterestFor([2,7,9,10], purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI)
    }

    private func isConsentOutdated() -> Bool {
        let settings = UserDefaults.standard
        guard let tcString = settings.string(forKey: "IABTCF_TCString"), !tcString.isEmpty else {
            return false
        }

        // base64 alphabet used to store data in IABTCF string
        let base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

        // date is stored in digits 1..7 of the IABTCF string
        let dateSubstring = String(tcString[tcString.index(tcString.startIndex, offsetBy: 1)..<tcString.index(tcString.startIndex, offsetBy: 7)])

        // interpret date substring as base64-encoded integer value
        var timestamp: Int64 = 0

        for char in dateSubstring {
            if let value = base64.firstIndex(of: char) {
                timestamp = timestamp * 64 + Int64(value.utf16Offset(in: base64))
            }
        }

        // timestamp is given in deci-seconds, convert to milliseconds
        timestamp *= 100

        // compare with current timestamp to get age in days
        let daysAgo = (Int64(Date().timeIntervalSince1970 * 1000) - timestamp) / (1000 * 60 * 60 * 24)

        // delete TC string if age is over a year
        if daysAgo > 365 {
            settings.removeObject(forKey: "IABTCF_TCString")
            return true
        }

        return false
    }
}
