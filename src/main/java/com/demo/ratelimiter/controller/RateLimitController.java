package com.demo.ratelimiter.controller;


import com.demo.ratelimiter.config.RateLimitConfig;
import com.demo.ratelimiter.controller.dto.RateLimitConfigResponse;
import com.demo.ratelimiter.controller.dto.RateLimitConfigUpdateRequest;
import com.demo.ratelimiter.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RateLimitController handles RESTful interactions for rate limiting.
 * It provides endpoints for configuring rate limits, checking token limits, and retrieving current rate limit configs.
 * Acts as a bridge to RateLimiterService.
 */
@RestController
@RequestMapping(RateLimitController.BASE_PATH)
public class RateLimitController {

    public static final String BASE_PATH = "/api/v1/rate-limit";
    private static final Logger log = LoggerFactory.getLogger(RateLimitController.class);
    private final RateLimitService rateLimiterService;

    @Autowired
    public RateLimitController(RateLimitService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(summary = "Configure rate limit settings",
            description = "Updates the rate limiting settings with specified calls per interval and interval duration.")
    @PostMapping("/configure")
    public ResponseEntity<?> configure(@Valid @RequestBody RateLimitConfigUpdateRequest configRequest) {
        log.info("Configuring rate limits: calls={} per interval, interval={} seconds",
                configRequest.getCalls(), configRequest.getInterval());
        rateLimiterService.updateRateLimitConfig(configRequest);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Check rate limit status for a token",
            description = "Determines whether requests from the specified token are currently rate limited.")
    @GetMapping("/is_rate_limited/{token}")
    public boolean isRateLimited(@PathVariable String token) {
        log.debug("Rate limit check started for token: {}", token);
        boolean isRateLimitedResult = rateLimiterService.isRateLimited(token);
        log.debug("Rate limit check result for token: {} - {}", token, isRateLimitedResult);
        return isRateLimitedResult;
    }

    @Operation(summary = "Retrieve current rate limit configuration",
            description = "Gets the current configuration settings for rate limiting.")
    @GetMapping("/config")
    public RateLimitConfigResponse getConfig() {
        log.info("Retrieving the current rate limit configuration.");
        RateLimitConfig config = rateLimiterService.getCurrentRateLimitConfig();
        RateLimitConfigResponse configDTO = new RateLimitConfigResponse();
        configDTO.setCalls(config.getCalls());
        configDTO.setInterval(config.getInterval());
        return configDTO;
    }
}



