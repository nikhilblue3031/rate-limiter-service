package com.demo.ratelimiter.controller.dto;

import lombok.Data;

/**
 * Represents the response structure for rate limit configuration, detailing
 * the allowed number of calls and the duration of the interval in seconds.
 */
@Data
public class RateLimitConfigResponse {
    private int calls; // Number of allowed calls per interval
    private long interval; // Duration of interval in seconds
}
