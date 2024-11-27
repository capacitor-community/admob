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
                        "isConsentFormAvailable": UMPConsentInformation.sharedInstance.formStatus == UMPFormStatus.available
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

                    if UMPConsentInformation.sharedInstance.consentStatus == UMPConsentStatus.required {
                        form?.present(from: rootViewController, completionHandler: { dismissError in
                            if dismissError != nil {
                                call.reject(dismissError?.localizedDescription ?? "Consent dismiss error")
                                return
                            }

                            call.resolve([
                                "status": self.getConsentStatusString(UMPConsentInformation.sharedInstance.consentStatus)
                            ])
                        })
                    } else {
                        call.resolve([
                            "status": self.getConsentStatusString(UMPConsentInformation.sharedInstance.consentStatus)
                        ])
                    }
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
}
