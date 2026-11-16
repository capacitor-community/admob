import Foundation
import Darwin

/// Ad cache lifetime measured independently of wall-clock changes, including device sleep.
final class AdExpiration {
    private let loadedAt: TimeInterval
    private let maxAge: TimeInterval
    private let clock: () -> TimeInterval

    private static let timebase: mach_timebase_info_data_t = {
        var info = mach_timebase_info_data_t()
        mach_timebase_info(&info)
        return info
    }()

    static func elapsedTime() -> TimeInterval {
        return TimeInterval(mach_continuous_time()) * TimeInterval(timebase.numer) / TimeInterval(timebase.denom) / 1_000_000_000
    }

    init(maxAge: TimeInterval, clock: @escaping () -> TimeInterval = AdExpiration.elapsedTime) {
        self.clock = clock
        self.loadedAt = clock()
        self.maxAge = maxAge
    }

    var isExpired: Bool {
        let age = clock() - loadedAt
        return age < 0 || age >= maxAge
    }
}
