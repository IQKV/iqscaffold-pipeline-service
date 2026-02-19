package com.iqscaffold.pipelineservice.shared.test;

import com.iqscaffold.pipelineservice.config.TestJacksonConfiguration;
import com.iqscaffold.pipelineservice.config.TestWebClientConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for integration tests.
 * Provides common configuration for all integration tests.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
    }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.com.iqscaffold=DEBUG",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri="
})
@Import({TestWebClientConfiguration.class, TestJacksonConfiguration.class})
public abstract class BaseIntegrationTest {
  // Common test configuration and utilities can be added here
}
