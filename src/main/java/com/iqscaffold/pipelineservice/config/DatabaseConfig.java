package com.iqscaffold.pipelineservice.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for Pipeline Service. Configures JPA repositories, entity scanning, and transaction management.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.iqscaffold.pipelineservice"
    },
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EntityScan(basePackages = {
    "com.iqscaffold.pipelineservice"
})
@EnableTransactionManagement
public class DatabaseConfig {
  // Entities and repositories are organized by domain modules
}
