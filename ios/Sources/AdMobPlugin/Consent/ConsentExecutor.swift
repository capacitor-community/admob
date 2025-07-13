import Foundation
import Capacitor
import GoogleMobileAds
import UserMessagingPlatform

class ConsentExecutor: NSObject {
    weak var plugin: AdMobPlugin?

    func requestConsentInfo(_ call: CAPPluginCall, _ debugGeography: Int, _ testDeviceIdentifiers: [String], _ tagForUnderAgeOfConsent: Bool) {
        let parameters = RequestParameters()
        let debugSettings = DebugSettings()

        debugSettings.geography = DebugGeography(rawValue: debugGeography) ?? DebugGeography.disabled
        debugSettings.testDeviceIdentifiers = testDeviceIdentifiers

        parameters.debugSettings = debugSettings
        parameters.isTaggedForUnderAgeOfConsent = tagForUnderAgeOfConsent

        // Request an update to the consent information.
        ConsentInformation.shared.requestConsentInfoUpdate(
            with: parameters,
            completionHandler: { error in
                if error != nil {
                    call.reject("Request consent info failed")
                } else {
                    call.resolve([
                        "status": self.getConsentStatusString(ConsentInformation.shared.consentStatus),
                        "isConsentFormAvailable": ConsentInformation.shared.formStatus == FormStatus.available
                    ])
                }
            })
    }

    func showConsentForm(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            let formStatus = ConsentInformation.shared.formStatus

            if formStatus == FormStatus.available {
                ConsentForm.load(with: {form, loadError in
                    if loadError != nil {
                        call.reject(loadError?.localizedDescription ?? "Load consent form error")
                        return
                    }

                    if ConsentInformation.shared.consentStatus == ConsentStatus.required {
                        form?.present(from: rootViewController, completionHandler: { dismissError in
                            if dismissError != nil {
                                call.reject(dismissError?.localizedDescription ?? "Consent dismiss error")
                                return
                            }

                            call.resolve([
                                "status": self.getConsentStatusString(ConsentInformation.shared.consentStatus)
                            ])
                        })
                    } else {
                        call.resolve([
                            "status": self.getConsentStatusString(ConsentInformation.shared.consentStatus)
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
        ConsentInformation.shared.reset()
        call.resolve()
    }

    func getConsentStatusString(_ consentStatus: ConsentStatus) -> String {
        switch consentStatus {
        case ConsentStatus.required:
            return "REQUIRED"
        case ConsentStatus.notRequired:
            return "NOT_REQUIRED"
        case ConsentStatus.obtained:
            return "OBTAINED"
        default:
            return "UNKNOWN"
        }
    }
}
