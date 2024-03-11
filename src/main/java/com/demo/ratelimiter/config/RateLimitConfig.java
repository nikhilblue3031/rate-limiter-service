package com.demo.ratelimiter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures rate limiting parameters: calls per interval and interval length.
 */
@ConfigurationProperties(prefix = "ratelimit")
@Data
@AllArgsConstructor
public class RateLimitConfig {
    private int calls; // Allowed requests per interval
    private long interval; // Interval duration in seconds
}

