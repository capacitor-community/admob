import Foundation

struct NativeAdPlacementValue {
    let feedId: String
    let slotKey: String
    let generation: Int
    let rect: CGRect
    let clip: CGRect

    init?(_ value: [String: Any], expectedFeedId: String) {
        guard
            value["visible"] as? Bool == true,
            let feedId = value["feedId"] as? String,
            feedId == expectedFeedId,
            let slotKey = value["slotKey"] as? String,
            let generation = (value["generation"] as? NSNumber)?.intValue,
            let rectValue = value["rect"] as? [String: Any],
            let clipValue = value["clipRect"] as? [String: Any],
            let rect = Self.rect(rectValue),
            let clip = Self.rect(clipValue),
            rect.width > 0,
            rect.height > 0,
            clip.width > 0,
            clip.height > 0
        else { return nil }

        self.feedId = feedId
        self.slotKey = slotKey
        self.generation = generation
        self.rect = rect
        self.clip = clip
    }

    private static func rect(_ value: [String: Any]) -> CGRect? {
        guard
            let originX = (value["x"] as? NSNumber)?.doubleValue,
            let originY = (value["y"] as? NSNumber)?.doubleValue,
            let width = (value["width"] as? NSNumber)?.doubleValue,
            let height = (value["height"] as? NSNumber)?.doubleValue
        else { return nil }
        return CGRect(x: originX, y: originY, width: width, height: height)
    }
}
