# Rate Limiter Service

This Rate Limiter Service is a RESTful API designed to manage and enforce API rate limits, ensuring clients adhere to specified request limits within defined time intervals. Built with Spring Boot, it provides endpoints for setting rate limits, checking rate limit status, and retrieving current rate limit configurations.

## Tech Stack
- **Spring Boot**
- **Docker**
- **REST APIs**
- **Java 17**
- **Gradle**
- **Junit**
- **Swagger**

## Features
- **Dynamic Rate Limit Configuration:** Allows administrators to set and adjust rate limits in real-time.
- **Rate Limit Status Check:** Clients can verify if their access is currently rate-limited based on their unique token.
- **Current Configuration Retrieval:** Fetch current rate limiting parameters at any time.

## Prerequisites
- **Docker Desktop** installed on your machine.
- Docker desktop can be downloaded from here : https://www.docker.com/products/docker-desktop/

## Setup and Running Instructions
1. Clone the project repository:
   ```
   git clone
   ```
   
2. Navigate to the project directory:
   `cd rate-limiter-service`

3. Start the service using Docker Compose - Make sure Docker Desktop is running.
   - **Check for Docker version**: `docker --version`
   - **For Docker version 1.x**: Run `docker-compose up --build`
   - **Docker version 2.x or greater**: Run `docker compose up --build`
      This builds the Docker image and starts the service, making the API accessible at `http://localhost:8080`.

## API Endpoints
- **Configure Rate Limit:** `POST /api/v1/rate-limit/configure` allows setting the number of allowed requests and the time interval.
- **Check Rate Limit:** `GET /api/v1/rate-limit/is_rate_limited/{token}` checks if requests from a specified token are rate-limited.

  Added a new endpoint "Current Configuration Retrieval" that helps in both validating rate limiting behavior and providing a preliminary 
  check before changing rate limiting configurations. Provides real-time insight into current rate limit settings and provides visibility and control for API access management.
- **Current Configuration Retrieval:** `GET /api/v1/rate-limit/config` retrieves the current rate limit configuration.

## Unit Tests

This project includes some unit and integration tests to verify the functionality of the rate limiting features. 
Tests cover various scenarios, including dynamic configuration of rate limits, checking the rate limit status of tokens, 
and retrieving the current configuration settings. The tests make sure that the API behaves as expected under different conditions.

- **Controller Integration Tests:** These tests verify the API endpoints' behavior, making sure that the controller correctly handles 
   incoming requests and responds appropriately based on the current rate limit settings.
- **Service Class Unit Tests:** Focus on the business logic within the RateLimiterService class, 
  making sure that rate limiting calculations, configuration updates, and status checks are performed accurately.

## Test Functionality Scripts

To make is easier for the testing of rate-limiting behavior, I've provided scripts that simulate a series of API calls to the service. 
These scripts can be found in the "**test-scripts**" directory under the project directory and include:

- **rate_limit_test-single-token.sh**: This script sends multiple requests to the rate limiting endpoint using a single token and outputs the rate-limited status of each request.

- **rate_limit_test_multiple_tokens.sh**: This script sends requests for multiple tokens to test concurrent rate limiting across different client tokens.

To execute these scripts, run the following commands from the project root directory:

Make sure the scripts have execute permission before running them. You may need to grant execute permissions using:

```
chmod +x ./test-scripts/*.sh
```

For testing a single token's rate limit:

```
./test-scripts/rate_limit_test-single-token.sh
```

For testing rate limits across multiple tokens:

```
./test-scripts/rate_limit_test_multiple_tokens.sh
```

These scripts are preset with some default configurations, such as the number of requests, interval, and token values. 
They automatically report each request's status, indicating whether it was allowed or rate-limited based on the current rate-limiting 
configuration of the service. This facilitates understanding and verifying the rate limiter's behavior under different 
conditions and configurations.The configurations in the scripts can be easily updated and re-run for specific testing needs, 
which allows for flexible and run different tests of the rate limiting functionality. 


## Testing through Swagger UI
Swagger UI
The Swagger UI provides an interactive interface to explore and test the Rate Limiter Service API directly from your browser.

Here's how to use it:

- Access the Swagger UI at: http://localhost:8082/swagger-ui/index.html.
- Utilize the "Try it out" feature to send requests to the API, modify parameters, and see the responses in real-time.
- Swagger displays both the RateLimitConfigRequest and RateLimitConfigResponse schemas, 
providing the acceptable range of values for requests per interval (2 to 1000) and interval duration in seconds (5 to 86400, equivalent to 24 hours).

## Logs
Once the application is started and running, logs directory and file should be generated under the root directory.
