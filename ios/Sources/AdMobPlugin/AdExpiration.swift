import Foundation

/// Ad cache lifetime measured independently of wall-clock changes.
final class AdExpiration {
    private let loadedAt: TimeInterval
    private let maxAge: TimeInterval
    private let clock: () -> TimeInterval

    init(maxAge: TimeInterval, clock: @escaping () -> TimeInterval = { ProcessInfo.processInfo.systemUptime }) {
        self.clock = clock
        self.loadedAt = clock()
        self.maxAge = maxAge
    }

    var isExpired: Bool {
        let age = clock() - loadedAt
        return age < 0 || age >= maxAge
    }
}
