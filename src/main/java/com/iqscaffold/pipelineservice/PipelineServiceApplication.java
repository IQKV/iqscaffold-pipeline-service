package com.iqscaffold.pipelineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.DependsOn;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.iqscaffold.pipelineservice.config")
@DependsOn("systemLiquibaseInitializer")
public class PipelineServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PipelineServiceApplication.class, args);
  }
}
