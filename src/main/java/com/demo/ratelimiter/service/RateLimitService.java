package com.demo.ratelimiter.service;


import com.demo.ratelimiter.config.RateLimitConfig;
import com.demo.ratelimiter.controller.dto.RateLimitConfigUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controls rate limiting of client requests based on configurable limits.
 * It ensures thread-safe operations using locks and dynamically calculates rate limiting windows,
 * enabling a flexible and scalable request rate. Supports updating rate limits, querying current configuration
 * and resetting state for testing or changing configuration.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private final RateLimitConfig rateLimitConfig;

    // Holds locks for each token to synchronize access in a concurrent environment.
    private final ConcurrentHashMap<String, Object> tokenLocks = new ConcurrentHashMap<>();


    // Outer map: Tracks rate limiting data for each token with a concurrent map to manage time windows.
    // Inner map: Holds the count of requests in an AtomicInteger for each time window, keyed by window start timestamp.
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicInteger>> tokenAccessMap = new ConcurrentHashMap<>();


    /**
     * Constructs a RateLimitService with the given rate limit configuration.
     * @param rateLimitConfig Configuration parameters for rate limiting.
     */
    @Autowired
    public RateLimitService(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    /**
     * Determines if a request identified by the given token is rate limited.
     * @param token The identifier for the requester.
     * @return true if the request is rate limited; false otherwise.
     */
    public boolean isRateLimited(String token) {
        try {
            tokenLocks.computeIfAbsent(token, k -> new Object());
            synchronized (tokenLocks.get(token)) {
                long currentWindowKey = getCurrentWindowKey();
                ConcurrentHashMap<Long, AtomicInteger> windows = tokenAccessMap.computeIfAbsent(token, k -> new ConcurrentHashMap<>());
                AtomicInteger currentCount = windows.computeIfAbsent(currentWindowKey, k -> new AtomicInteger(0));

                if (currentCount.incrementAndGet() > rateLimitConfig.getCalls()) {
                    // Rate limit exceeded
                    return true;
                } else {
                    // Remove old windows to prevent map bloat and focus on current data.
                    cleanupOldWindows(token, currentWindowKey);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("An unexpected error occurred while checking the rate limit for token: {}", token, e);
            //  Keeping a safe failure state - considering the request as rate limited.
            return true;
        }

    }
    /**
     * Calculates the key representing the start of the current rate limiting window.
     * This method aligns the current time to the nearest window start based on the configured interval.
     * For example, if the interval is set to 30 seconds, windows will start at times like :00 and :30 of each minute,
     * independent of when requests arrive.
     *
     * @return The timestamp marking the start of the current window.
     */
    private long getCurrentWindowKey() {
        long currentTimeMillis = System.currentTimeMillis();
        long intervalInMillis = rateLimitConfig.getInterval() * 1000;
        return (currentTimeMillis / intervalInMillis) * intervalInMillis;
    }

    /**
     * Removes entries for time windows that are no longer relevant to the current rate limiting window.
     * @param token The map of window start timestamps to request counts for a specific token.
     * @param currentWindowKey The key representing the start of the current window.
     */
    private void cleanupOldWindows(String token, long currentWindowKey) {
        ConcurrentHashMap<Long, AtomicInteger> windows = tokenAccessMap.get(token);
        if (null != windows) {
            windows.keySet().removeIf(windowKey -> windowKey < currentWindowKey);
        }
    }

    /**
     * Updates the configuration for rate limiting, adjusting the allowed request calls and interval.
     * @param rateLimitConfigRequest contains the new calls and interval values that needs to be updated.
     */
    public synchronized void updateRateLimitConfig(RateLimitConfigUpdateRequest rateLimitConfigRequest) {
        rateLimitConfig.setCalls(rateLimitConfigRequest.getCalls());
        rateLimitConfig.setInterval(rateLimitConfigRequest.getInterval());
    }

    /**
     * Retrieves the current rate limiting configuration.
     * @return The active RateLimitConfig instance.
     */
    public RateLimitConfig getCurrentRateLimitConfig() {
        return rateLimitConfig;
    }

    /**
     * Resets the rate limiter, clearing request counts and token locks.
     * Ensures a fresh state for tests.
     */
    public void resetRateLimiter() {
        // Clears request counts and token locks
        tokenAccessMap.clear();
        tokenLocks.clear();
    }
}