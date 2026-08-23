package com.getcapacitor.community.admob.nativeads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NativeAdFeedSessionsTest {

    @Test
    public void limitsFeedsAndAllowsSameFeedReplacement() {
        NativeAdFeedSessions sessions = new NativeAdFeedSessions();

        assertTrue(sessions.start("first", "session-1"));
        assertTrue(sessions.start("second", "session-2"));
        assertFalse(sessions.start("third", "session-3"));
        assertTrue(sessions.start("first", "replacement"));
        assertTrue(sessions.isCurrent("first", "replacement"));
    }

    @Test
    public void rejectsStalePlacementSequencesPerFeed() {
        NativeAdFeedSessions sessions = new NativeAdFeedSessions();
        sessions.start("feed", "session");

        assertTrue(sessions.accepts("feed", 1));
        assertFalse(sessions.accepts("feed", 1));
        assertFalse(sessions.accepts("feed", 0));
        assertTrue(sessions.accepts("feed", 2));
    }
}
