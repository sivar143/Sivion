package com.sivion.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Provides a shared RestClient builder for outbound HTTP integrations.
 *
 * Spring Boot does not expose a RestClient.Builder in every supported
 * application configuration, so register one explicitly for integrations
 * such as the Shiprocket tracking adapter.
 */
@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
