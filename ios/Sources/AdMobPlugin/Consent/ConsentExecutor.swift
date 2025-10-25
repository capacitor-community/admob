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
                        "isConsentFormAvailable": ConsentInformation.shared.formStatus == FormStatus.available,
                        "canRequestAds": ConsentInformation.shared.canRequestAds,
                        "privacyOptionsRequirementStatus": self.getPrivacyOptionsRequirementStatus(ConsentInformation.shared.privacyOptionsRequirementStatus)
                    ])
                }
            })
    }

    @MainActor
    func showPrivacyOptionsForm(_ call: CAPPluginCall) {
        guard let rootViewController = plugin?.getRootVC() else {
            return call.reject("No ViewController")
        }

        Task {
            do {
                try await ConsentForm.presentPrivacyOptionsForm(from: rootViewController)
                call.resolve()
            } catch {
                call.reject("Failed to show privacy options form: \(error.localizedDescription)")
            }
        }
    }

    func showConsentForm(_ call: CAPPluginCall) {
        if let rootViewController = plugin?.getRootVC() {
            let formStatus = ConsentInformation.shared.formStatus

            if formStatus == FormStatus.available {
                Task { @MainActor in
                    do {
                        try await ConsentForm.loadAndPresentIfRequired(from: rootViewController)

                        call.resolve([
                            "status": self.getConsentStatusString(ConsentInformation.shared.consentStatus),
                            "canRequestAds": ConsentInformation.shared.canRequestAds,
                            "privacyOptionsRequirementStatus": self.getPrivacyOptionsRequirementStatus(ConsentInformation.shared.privacyOptionsRequirementStatus)
                        ])
                    } catch {
                        call.reject("Request consent info failed")
                    }
                }
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

    func getPrivacyOptionsRequirementStatus(_ requirementStatus: PrivacyOptionsRequirementStatus) -> String {
        switch requirementStatus {
        case PrivacyOptionsRequirementStatus.required:
            return "REQUIRED"
        case PrivacyOptionsRequirementStatus.notRequired:
            return "NOT_REQUIRED"
        default:
            return "UNKNOWN"
        }
    }
}
