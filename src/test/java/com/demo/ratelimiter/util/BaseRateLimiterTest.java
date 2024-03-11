package com.demo.ratelimiter.util;

import com.demo.ratelimiter.controller.ConcurrentRateLimiterTest;
import com.demo.ratelimiter.controller.dto.RateLimitConfigUpdateRequest;
import com.demo.ratelimiter.service.RateLimitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseRateLimiterTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RateLimitService rateLimiterService;

    protected ExecutorService service;

    @BeforeEach
    public void setUp() {
        // Reset the rate limiter before each test
        rateLimiterService.resetRateLimiter();
        // Initialize ExecutorService only if needed.
        if (this instanceof ConcurrentRateLimiterTest) {
            service = Executors.newFixedThreadPool(20);
        }
    }


    // Commonly used URLs or endpoints
    protected static final String BASE_PATH = "/api/v1/rate-limit";
    protected static final String CONFIGURE_ENDPOINT = BASE_PATH + "/configure";
    protected static final String IS_RATE_LIMITED_ENDPOINT = BASE_PATH + "/is_rate_limited/";
    protected static final String GET_CONFIG_ENDPOINT = BASE_PATH + "/config";


    /**
     * Creates a JSON string representation of {@link RateLimitConfigUpdateRequest} with provided calls and interval.
     *
     * @param calls    the number of calls allowed per interval
     * @param interval the interval duration in seconds
     * @return JSON string of {@link RateLimitConfigUpdateRequest}
     * @throws JsonProcessingException if JSON writing process fails
     */
    protected static String getRequestJson(int calls, int interval) throws JsonProcessingException {
        RateLimitConfigUpdateRequest configRequest = new RateLimitConfigUpdateRequest(calls, interval);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(configRequest);
    }

    /**
     * Makes a POST request to configure the rate limit with the given JSON payload.
     * Validates the request was processed successfully by expecting an HTTP 200 OK status code.
     *
     * @param requestJson JSON string of {@link RateLimitConfigUpdateRequest} representing the configuration to be set
     * @throws Exception if the MockMvc request performing process fails
     */
    protected void configureSuccessfulRateLimit(String requestJson) throws Exception {
        mockMvc.perform(post(CONFIGURE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}

