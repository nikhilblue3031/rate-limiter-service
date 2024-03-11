package com.demo.ratelimiter.controller;


import com.demo.ratelimiter.util.BaseRateLimiterTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ConcurrentRateLimiterTest extends BaseRateLimiterTest {

    @Test
    @DisplayName("Verify concurrent access does not exceed rate limit")
    public void testConcurrentAccess() throws Exception {
        //Arrange
        String requestJson = getRequestJson(10, 5);
        configureSuccessfulRateLimit(requestJson);

        // Action and Assert
        List<CompletableFuture<Boolean>> futures = generateRequests("testToken", 20);
        assertRequestResults(futures, 10);
    }

    @Test
    @DisplayName("Test - rate limiting works independently for different tokens under high concurrency")
    public void testConcurrentAccess_ForDifferentTokens() throws Exception {
        // Arrange
        String requestJson = getRequestJson(5, 45);
        configureSuccessfulRateLimit(requestJson);

        // Action and Assert
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String token = "testToken" + i;
            futures.addAll(generateRequests(token, 5));
        }
        assertRequestResults(futures, 50);
    }

    @Test
    @DisplayName("Test - exceeding rate limit with multiple requests for different tokens")
    public void testExceedingRateLimit_ForDifferentTokens() throws Exception {
        // Arrange
        String requestJson = getRequestJson(5, 45);
        configureSuccessfulRateLimit(requestJson);

        // Action and Assert
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String token = "testToken" + i;
            futures.addAll(generateRequests(token, 7)); // 2 more requests than allowed to test rate limiting
        }
        assertRequestResults(futures, 25); // Expect 5 successful requests per token before rate limiting
    }

    /**
     * Generates a list of CompletableFuture representing asynchronous requests for a provided token.
     * Each CompletableFuture will complete with 'true' if the request was rate limited,
     * 'false' if it was not rate limited.
     *
     * @param token The token to be used for the requests.
     * @param count The number of requests to generate.
     * @return A list of CompletableFuture<Boolean> for the requests.
     */
    private List<CompletableFuture<Boolean>> generateRequests(String token, int count) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> makeRequest(token), service);
            futures.add(future);
        }
        return futures;
    }

    /**
     * Asserts a list of CompletableFuture<Boolean> to confirm the expected number of successful requests.
     * A successful request is indicated by 'false', implying that it was not rate-limited.
     * 'true' indicates that the request was rate-limited.
     * This method compares the number of 'false' values (non-rate-limited requests) in completed futures
     * with the expected number of successful requests.
     *
     * @param futures           The list of futures to assert results for.
     * @param expectedSuccesses The expected number of successful (not rate limited) requests.
     */
    private void assertRequestResults(List<CompletableFuture<Boolean>> futures, int expectedSuccesses) {
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allDone.join(); // Wait for all futures to complete

        long successfulRequests = futures.stream().map(CompletableFuture::join).filter(b -> b).count();
        assertEquals(expectedSuccesses, successfulRequests, "Expected number of successful requests does not match");
    }

    /**
     * This method submits a request to the rate limiting service and returns a Boolean indicating its status.
     * A 'true' response indicates a rate-limited request, while a 'false' response indicates the request was not rate limited.
     * Exceptions are detected and handled as rate-limited cases, returning 'true'.
     *
     * @param token The token for which the rate limit status is being checked.
     * @return Boolean indicating whether the request was rate limited ('true') or not ('false').
     */
    private Boolean makeRequest(String token) {
        try {
            MvcResult result = mockMvc.perform(get(IS_RATE_LIMITED_ENDPOINT + token)).andReturn();
            // 'true' if not rate limited, 'false' if rate limited
            return !Boolean.parseBoolean(result.getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Consider as rate limited on exception
        }
    }

}

