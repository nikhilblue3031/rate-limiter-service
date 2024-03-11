package com.demo.ratelimiter.service;


import com.demo.ratelimiter.config.RateLimitConfig;
import com.demo.ratelimiter.controller.dto.RateLimitConfigUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitServiceTest {

    private RateLimitService rateLimiterService;

    @BeforeEach
    public void setUp() {
        // Arrange
        RateLimitConfig rateLimitConfig = new RateLimitConfig(10, 60);
        rateLimiterService = new RateLimitService(rateLimitConfig);
    }

    @Test
    @DisplayName("Test token is not rate limited under threshold")
    public void testIsRateLimited_PositiveCase() {
        // Act
        boolean result = rateLimiterService.isRateLimited("testToken");
        // Assert
        assertFalse(result, "Token should not be rate limited when under threshold.");
    }

    @Test
    @DisplayName("Test token is rate limited when exceeding threshold")
    public void testIsRateLimited_NegativeCase() {
        // Arrange
        for (int i = 0; i < 12; i++) {
            rateLimiterService.isRateLimited("testToken");
        }
        // Act
        boolean result = rateLimiterService.isRateLimited("testToken");
        // Assert
        assertTrue(result, "Token should be rate limited when exceeding threshold.");
    }

    @Test
    @DisplayName("Test calculation of current window key")
    public void testGetCurrentWindowKey() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Arrange - Get private method via reflection
        Method method = RateLimitService.class.getDeclaredMethod("getCurrentWindowKey");
        method.setAccessible(true);
        // Act
        long result = (long) method.invoke(rateLimiterService);
        // Assert
        assertEquals(0, result % 60000, "The window key should be a multiple of 60 seconds.");
    }

    @Test
    @DisplayName("Test cleanup of old windows")
    @SuppressWarnings("unchecked") // Suppress the unchecked cast warning
    public void testCleanupOldWindows() throws Exception {
        // Arrange
        String testToken = "testToken";
        long currentWindowKey = System.currentTimeMillis();
        long oldWindowKey1 = currentWindowKey - 20000; // Calculating an old window
        long oldWindowKey2 = currentWindowKey - 10000; // Calculating an old window

        // Reflection to access the tokenAccessMap
        Field tokenAccessMapField = RateLimitService.class.getDeclaredField("tokenAccessMap");
        tokenAccessMapField.setAccessible(true);
        ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicInteger>> tokenAccessMap =
                (ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicInteger>>) tokenAccessMapField.get(rateLimiterService);

        // Populate tokenAccessMap with test data
        ConcurrentHashMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();
        windows.put(oldWindowKey1, new AtomicInteger(2));
        windows.put(oldWindowKey2, new AtomicInteger(3));
        windows.put(currentWindowKey, new AtomicInteger(1)); // Current window
        tokenAccessMap.put(testToken, windows);

        // Access the private method cleanupOldWindows using reflection
        Method cleanupMethod = RateLimitService.class.getDeclaredMethod("cleanupOldWindows", String.class, long.class);
        cleanupMethod.setAccessible(true);

        // Act
        cleanupMethod.invoke(rateLimiterService, testToken, currentWindowKey);

        // Assert
        ConcurrentHashMap<Long, AtomicInteger> updatedWindows = tokenAccessMap.get(testToken);
        assertEquals(1, updatedWindows.size(), "Only the current window should remain.");
        assertTrue(updatedWindows.containsKey(currentWindowKey), "The current window should remain.");
    }


    @Test
    @DisplayName("Test updating the rate limit configuration")
    public void testUpdateRateLimitConfig() {
        // Act
        rateLimiterService.updateRateLimitConfig(new RateLimitConfigUpdateRequest(5,30));

        // Assert
        RateLimitConfig config = rateLimiterService.getCurrentRateLimitConfig();
        assertEquals(5, config.getCalls(), "Calls per interval should be updated to 5.");
        assertEquals(30, config.getInterval(), "Interval in seconds should be updated to 30.");
    }

    @Test
    @DisplayName("Test retrieving the current rate limit configuration")
    public void testGetCurrentRateLimitConfig() {
        // Act
        RateLimitConfig config = rateLimiterService.getCurrentRateLimitConfig();

        // Assert
        assertNotNull(config, "Rate limit configuration should not be null.");
        assertEquals(10, config.getCalls(), "Calls per interval should match the initial setup.");
        assertEquals(60, config.getInterval(), "Interval in seconds should match the initial setup.");
    }

}
