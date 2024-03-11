package com.demo.ratelimiter.controller;


import com.demo.ratelimiter.util.BaseRateLimiterTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RateLimitControllerTest extends BaseRateLimiterTest {

    @Test
    @DisplayName("Test configuring rate limit successfully")
    public void testConfigureRateLimit() throws Exception {
        //Arrange
        String requestJson = getRequestJson(10, 45);

        // Action and Assert
        // Configure the rate limit
        configureSuccessfulRateLimit(requestJson);

        // Fetching the current configuration to verify updates
        mockMvc.perform(get(GET_CONFIG_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calls").value(10))
                .andExpect(jsonPath("$.interval").value(45));
    }

    @Test
    @DisplayName("Test request rate limiting with configured limits")
    public void testIsRateLimited() throws Exception {

        //Arrange
        String requestJson = getRequestJson(3, 20);

        //Action and Assert
        // Configure the rate limit
        configureSuccessfulRateLimit(requestJson);

        // Make allowed number of requests and assert not rate limited
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(IS_RATE_LIMITED_ENDPOINT + "testToken"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        // Make additional request and assert rate limited
        //Assuming the server executes the api calls very quick.
        mockMvc.perform(get(IS_RATE_LIMITED_ENDPOINT + "testToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Optionally - Wait for interval to reset and test again
        // Wait longer than the rate limit interval to ensure it's reset.
        Thread.sleep(21000);
        // This slows down the test as the process sleeps for the time configured.

        // Repeat request and assert not rate limited again
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(IS_RATE_LIMITED_ENDPOINT + "testToken"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        // Make additional request and assert rate limited again, if needed
        mockMvc.perform(get(IS_RATE_LIMITED_ENDPOINT + "testToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Test rate limit configuration with invalid number of calls")
    public void testConfigureRateLimitWithInvalidCalls() throws Exception {
        //Arrange : Create a configuration request with an invalid interval value
        String requestJson = getRequestJson(0, 30);

        //Action and Assert : Attempt to configure the rate limiter and expect a 400 Bad Request response
        mockMvc.perform(post(CONFIGURE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test rate limit configuration with invalid interval value")
    public void testConfigureRateLimitWithInvalidInterval() throws Exception {
        // Arrange: Create a configuration request with an invalid interval value
        String requestJson = getRequestJson(5, 1);

        // Action and Assert: Attempt to configure the rate limiter and expect a 400 Bad Request response
        mockMvc.perform(post(CONFIGURE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}

