package com.iqscaffold.pipelineservice.config;

import javax.sql.DataSource;

import com.iqscaffold.pipelineservice.tenancy.SchemaNameResolver;
import com.iqscaffold.pipelineservice.tenancy.SchemaPerTenantConnectionProvider;
import com.iqscaffold.pipelineservice.tenancy.SchemaTenantIdentifierResolver;
import com.iqscaffold.pipelineservice.tenancy.TenantLiquibaseRunner;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test configuration for single-tenant (non-multi-tenant) support in tests.
 * Disables multi-tenancy and uses H2's PUBLIC schema.
 */
@TestConfiguration
@Profile("test")
public class TestTenantConfiguration {

  @Bean
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
    // Disable multi-tenancy for tests - use single schema (PUBLIC)
    return hibernateProperties -> {
      hibernateProperties.put("hibernate.multiTenancy", "NONE");
      hibernateProperties.put(AvailableSettings.USE_SQL_COMMENTS, true);
    };
  }

  @Bean
  public TenantLiquibaseRunner tenantLiquibaseRunner() {
    return org.mockito.Mockito.mock(TenantLiquibaseRunner.class);
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
