package com.demo.ratelimiter;

import com.demo.ratelimiter.config.RateLimitConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for the Rate Limiter Service.
 * It enables configuration properties and starts the Spring Boot application.
 */
@SpringBootApplication
@EnableConfigurationProperties({RateLimitConfig.class})
public class RateLimiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApplication.class, args);
    }

}
