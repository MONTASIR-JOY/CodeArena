package com.kuet.codearena.data.remote;

import android.os.SystemClock;

/**
 * Thread-safe rate limiter guaranteeing a minimum delay between external API requests.
 */
public class RateLimiter {
    private static RateLimiter instance;
    private final long minIntervalMs;
    private long lastExecutionTimeMs = 0;

    public RateLimiter(long minIntervalMs) {
        this.minIntervalMs = minIntervalMs;
    }

    public static synchronized RateLimiter getCfInstance() {
        if (instance == null) {
            instance = new RateLimiter(2000); // 2000ms minimum interval for Codeforces
        }
        return instance;
    }

    /**
     * Blocks calling thread if necessary to ensure at least minIntervalMs has passed.
     */
    public synchronized void acquire() {
        long now = SystemClock.elapsedRealtime();
        long elapsed = now - lastExecutionTimeMs;
        if (elapsed < minIntervalMs) {
            long waitTime = minIntervalMs - elapsed;
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastExecutionTimeMs = SystemClock.elapsedRealtime();
    }
}
