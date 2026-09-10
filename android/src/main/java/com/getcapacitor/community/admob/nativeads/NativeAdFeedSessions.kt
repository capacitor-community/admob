package com.getcapacitor.community.admob.nativeads

internal class NativeAdFeedSessions {
    private val sessions = mutableMapOf<String, String>()
    private val placementSequences = mutableMapOf<String, Long>()

    fun start(feedId: String, sessionId: String): Boolean {
        if (feedId !in sessions && sessions.size >= MAX_FEEDS) return false
        sessions[feedId] = sessionId
        placementSequences[feedId] = -1
        return true
    }

    fun isCurrent(feedId: String, sessionId: String): Boolean = sessions[feedId] == sessionId

    fun accepts(feedId: String, sequence: Long): Boolean {
        if (sequence <= placementSequences.getOrDefault(feedId, -1)) return false
        placementSequences[feedId] = sequence
        return true
    }

    fun remove(feedId: String) {
        sessions.remove(feedId)
        placementSequences.remove(feedId)
    }

    fun clear() {
        sessions.clear()
        placementSequences.clear()
    }

    private companion object {
        const val MAX_FEEDS = 2
    }
}
