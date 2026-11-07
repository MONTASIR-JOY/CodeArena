package com.codearena.data.api;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Throttles outgoing HTTP requests to comply with Codeforces API rate limits (1 req / 2s).
 */
public class RateLimitInterceptor implements Interceptor {
    private final long minIntervalMillis;
    private long lastRequestTime = 0;
    private final Object lock = new Object();

    public RateLimitInterceptor(long minIntervalMillis) {
        this.minIntervalMillis = minIntervalMillis;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRequestTime;
            if (elapsed < minIntervalMillis) {
                try {
                    Thread.sleep(minIntervalMillis - elapsed);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestTime = System.currentTimeMillis();
        }
        return chain.proceed(chain.request());
    }
}

