import Capacitor
import Foundation

enum NativeAdValues {
    static func session(_ call: CAPPluginCall) -> (feedId: String, sessionId: String)? {
        guard let feedId = requiredString(call, "feedId"), let sessionId = requiredString(call, "sessionId") else {
            return nil
        }
        return (feedId, sessionId)
    }

    static func requiredString(_ call: CAPPluginCall, _ name: String) -> String? {
        guard let value = call.getString(name)?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else {
            call.reject("\(name) must not be empty")
            return nil
        }
        return value
    }

    static func key(_ feedId: String, _ slotKey: String) -> String {
        "\(feedId)\u{0}\(slotKey)"
    }

    static func identity(_ feedId: String, _ sessionId: String, _ slotKey: String) -> [String: Any] {
        ["feedId": feedId, "sessionId": sessionId, "slotKey": slotKey]
    }
}
