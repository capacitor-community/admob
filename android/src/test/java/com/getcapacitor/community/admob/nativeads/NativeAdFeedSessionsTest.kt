package com.getcapacitor.community.admob.nativeads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeAdFeedSessionsTest {
    @Test
    fun limitsFeedsAndAllowsSameFeedReplacement() {
        val sessions = NativeAdFeedSessions()

        assertTrue(sessions.start("first", "session-1"))
        assertTrue(sessions.start("second", "session-2"))
        assertFalse(sessions.start("third", "session-3"))
        assertTrue(sessions.start("first", "replacement"))
        assertTrue(sessions.isCurrent("first", "replacement"))
    }

    @Test
    fun rejectsStalePlacementSequencesPerFeed() {
        val sessions = NativeAdFeedSessions()
        sessions.start("feed", "session")

        assertTrue(sessions.accepts("feed", 1))
        assertFalse(sessions.accepts("feed", 1))
        assertFalse(sessions.accepts("feed", 0))
        assertTrue(sessions.accepts("feed", 2))
    }
}
