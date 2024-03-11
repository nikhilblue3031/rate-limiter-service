package com.demo.ratelimiter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
 * This class defines a custom OpenAPI configuration for the Rate Limiter API.
 */
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("Rate Limiter API")
                    .description("A simple rate limiter service to manage API request traffic.")
                    .version("1.0"));
}
}