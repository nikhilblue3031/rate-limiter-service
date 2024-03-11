package com.demo.ratelimiter.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Configuration request payload for rate limit settings, specifying allowed calls per interval and interval length.
 */
@Data
@AllArgsConstructor
public class RateLimitConfigUpdateRequest {
    @Min(2) // Enforces the minimum value of 1 for calls
    @Max(1000) // Enforces the maximum value of 1000 for calls
    @Schema(description = "Number of calls allowed per interval. Value must be between 1 and 1000.", example = "100")
    private int calls;

    @Min(5) // Enforces the minimum value of 5 seconds for the interval
    @Max(86400) // Enforces the maximum value of 86400 seconds (24 hours) for the interval
    @Schema(description = "Interval duration in seconds. Value must be between 5 and 86400 (24 hours).", example = "30")
    private long interval;
}

