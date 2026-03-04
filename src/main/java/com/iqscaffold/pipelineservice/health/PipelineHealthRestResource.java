package com.iqscaffold.pipelineservice.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public health check endpoint for pipeline service.
 * Used by frontend to monitor service availability and display degradation banners.
 * 
 * <p>This endpoint is intentionally public (no authentication required) to allow
 * health monitoring even when authentication services are degraded.</p>
 */
@RestController
@RequestMapping("/api/v1/pipeline")
@Tag(name = "Pipeline Health", description = "Public health check endpoint for pipeline service monitoring")
public class PipelineHealthRestResource {

  @Operation(
      summary = "Pipeline service health check",
      description = "Returns health status of the pipeline management service")
  @ApiResponse(responseCode = "200", description = "Pipeline service is healthy")
  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "service", "pipeline",
        "timestamp", Instant.now().toString()
    ));
  }
}
