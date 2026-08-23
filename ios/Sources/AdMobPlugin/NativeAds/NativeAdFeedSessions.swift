import Foundation

final class NativeAdFeedSessions {
    private let maxFeeds = 2
    private var sessions: [String: String] = [:]
    private var placementSequences: [String: Int] = [:]

    func start(feedId: String, sessionId: String) -> Bool {
        guard sessions[feedId] != nil || sessions.count < maxFeeds else { return false }
        sessions[feedId] = sessionId
        placementSequences[feedId] = -1
        return true
    }

    func isCurrent(feedId: String, sessionId: String) -> Bool {
        sessions[feedId] == sessionId
    }

    func accepts(feedId: String, sequence: Int) -> Bool {
        guard sequence > (placementSequences[feedId] ?? -1) else { return false }
        placementSequences[feedId] = sequence
        return true
    }

    func remove(feedId: String) {
        sessions.removeValue(forKey: feedId)
        placementSequences.removeValue(forKey: feedId)
    }

    func removeAll() {
        sessions.removeAll()
        placementSequences.removeAll()
    }
}
