package com.iqscaffold.pipelineservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Test configuration for Jackson ObjectMapper.
 * Provides Jackson2ObjectMapperBuilder bean for test contexts.
 */
@TestConfiguration
public class TestJacksonConfiguration {

  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder();
  }
}
