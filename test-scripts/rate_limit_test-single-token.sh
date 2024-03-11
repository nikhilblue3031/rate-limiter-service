#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:8082/api/v1/rate-limit"


# Endpoints
CONFIG_ENDPOINT="$BASE_URL/config"
RATE_LIMITED_ENDPOINT="$BASE_URL/is_rate_limited/testToken"

# Number of requests to send
REQUESTS=30

# Function to get and print current rate limit configuration
get_current_config() {
    echo "Fetching current rate limit configuration..."
    curl -s "$CONFIG_ENDPOINT"
    echo ""
}

# Function to perform rate limit check
check_rate_limit() {
    for ((i=1; i<=REQUESTS; i++))
    do
        # Capture the current timestamp
        TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
        RESPONSE=$(curl -s "$RATE_LIMITED_ENDPOINT")
        echo "$TIMESTAMP - Request $i: Rate limited - $RESPONSE"
        sleep 1 # Pause for a second between requests
    done
}

# Main
echo ""
echo "Starting rate limit check..."
echo ""
# Fetch and print the current rate limit configuration
echo ""
get_current_config
# Perform rate limit checks
echo ""
check_rate_limit
echo "Rate limit check completed."

