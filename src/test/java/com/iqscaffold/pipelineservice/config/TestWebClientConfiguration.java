package com.iqscaffold.pipelineservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Test configuration for WebClient.
 * Provides WebClient.Builder bean for test contexts.
 */
@TestConfiguration
public class TestWebClientConfiguration {

  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
