#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:8082/api/v1/rate-limit"

# Endpoints
CONFIG_ENDPOINT="$BASE_URL/config"
RATE_LIMITED_ENDPOINT="$BASE_URL/is_rate_limited"

# Tokens to test
TOKENS=("token1" "token2" "token3")

# Number of requests to send per token
REQUESTS=10

# Function to get and print current rate limit configuration
get_current_config() {
    echo "Fetching current rate limit configuration..."
    curl -s "$CONFIG_ENDPOINT"
    echo "" # New line for better readability
}

# Function to perform rate limit check for a single token
check_rate_limit_for_token() {
    local token=$1
    local results_file="rate_limit_$token.txt"
    > "$results_file" # Clear file contents before use
    for ((i=1; i<=REQUESTS; i++))
    do
        # Capture the current timestamp
        TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
        RESPONSE=$(curl -s "$RATE_LIMITED_ENDPOINT/$token")
        echo "$TIMESTAMP - Request $i: Rate limited - $RESPONSE" >> "$results_file"
        sleep 1 # Pause for a second between requests
    done
}

# Perform concurrent rate limit checks for all tokens
perform_concurrent_checks() {
    for token in "${TOKENS[@]}"
    do
        check_rate_limit_for_token "$token" &
    done
    wait # Wait for all background jobs to finish
}

# Display results for each token
display_results() {
    for token in "${TOKENS[@]}"
    do
        local results_file="rate_limit_$token.txt"
        echo "Results for $token:"
        cat "$results_file"
        echo "" # New line for spacing
        rm "$results_file" # Clean up file
    done
}

# Main
echo "Starting rate limit check..."
# Fetch and print the current rate limit configuration
get_current_config
# Perform concurrent requests for multiple tokens
perform_concurrent_checks
# Display the requests rate limiting results
display_results
echo "Rate limit check completed."
