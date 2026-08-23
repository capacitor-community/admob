package com.getcapacitor.community.admob.nativeads;

import java.util.HashMap;
import java.util.Map;

final class NativeAdFeedSessions {

    private static final int MAX_FEEDS = 2;

    private final Map<String, String> sessions = new HashMap<>();
    private final Map<String, Long> placementSequences = new HashMap<>();

    boolean start(String feedId, String sessionId) {
        if (!sessions.containsKey(feedId) && sessions.size() >= MAX_FEEDS) return false;
        sessions.put(feedId, sessionId);
        placementSequences.put(feedId, -1L);
        return true;
    }

    boolean isCurrent(String feedId, String sessionId) {
        return sessionId.equals(sessions.get(feedId));
    }

    boolean accepts(String feedId, long sequence) {
        if (sequence <= placementSequences.getOrDefault(feedId, -1L)) return false;
        placementSequences.put(feedId, sequence);
        return true;
    }

    void remove(String feedId) {
        sessions.remove(feedId);
        placementSequences.remove(feedId);
    }

    void clear() {
        sessions.clear();
        placementSequences.clear();
    }
}
