package com.premium;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Tiny in-memory limiter: 10 attempts per 10 minutes per key (used for login and sign-up). */
@Component
public class RateLimiter {
    private static final int MAX = 10;
    private static final long WINDOW_MS = 10 * 60_000L;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public void check(String key) {
        long now = System.currentTimeMillis();
        if (hits.size() > 10_000) hits.clear();                    // keep memory bounded
        Deque<Long> d = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (d) {
            while (!d.isEmpty() && now - d.peekFirst() > WINDOW_MS) d.pollFirst();
            if (d.size() >= MAX) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "too_many_attempts");
            d.addLast(now);
        }
    }
}
