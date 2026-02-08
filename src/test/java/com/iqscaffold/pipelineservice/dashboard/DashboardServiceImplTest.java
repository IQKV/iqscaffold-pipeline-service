package com.iqscaffold.pipelineservice.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iqscaffold.pipelineservice.config.IqScaffoldProperties;
import com.iqscaffold.pipelineservice.dashboard.dto.DashboardDtos;
import com.iqscaffold.pipelineservice.pipeline.PipelineItem;
import com.iqscaffold.pipelineservice.pipeline.PipelineItemRepository;
import com.iqscaffold.pipelineservice.pipeline.PipelineStage;
import com.iqscaffold.pipelineservice.pipeline.PipelineStageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceImplTest {

  @Mock
  private PipelineItemRepository pipelineItemRepository;

  @Mock
  private PipelineStageRepository stageRepository;

  @Mock
  private WebClient webClient;

  @Mock
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec responseSpec;

  @Mock
  private IqScaffoldProperties properties;

  @Mock
  private IqScaffoldProperties.Services services;

  private DashboardServiceImpl service;

  private PipelineStage newStage;
  private PipelineStage contactedStage;
  private PipelineStage wonStage;
  private PipelineStage lostStage;

  @BeforeEach
  void setUp() {
    service = new DashboardServiceImpl(pipelineItemRepository, stageRepository, webClient, properties);

    // Setup stages
    newStage = new PipelineStage("New", 0);
    newStage.setId(1L);

    contactedStage = new PipelineStage("Contacted", 1);
    contactedStage.setId(2L);

    wonStage = new PipelineStage("Won", 4);
    wonStage.setId(3L);
    wonStage.setIsFinalStage(true);

    lostStage = new PipelineStage("Lost", 5);
    lostStage.setId(4L);
    lostStage.setIsFinalStage(true);
  }

  @Test
  @DisplayName("Should get dashboard stats without date filter")
  void shouldGetDashboardStatsWithoutDateFilter() {
    // Arrange
    when(properties.getServices()).thenReturn(services);
    when(services.getLeadServiceUrl()).thenReturn("http://localhost:8081");

    List<PipelineItem> items = createTestItems();
    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    Map<String, Long> leadsBySource = new HashMap<>();
    leadsBySource.put("Website", 2L);
    leadsBySource.put("Referral", 1L);

    setupWebClientMock(leadsBySource);

    // Act
    DashboardDtos.DashboardStatsResponse result = service.getDashboardStats(null, null);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.totalLeads()).isEqualTo(4L);
    assertThat(result.wonLeads()).isEqualTo(1L);
    assertThat(result.lostLeads()).isEqualTo(1L);
    assertThat(result.activeLeads()).isEqualTo(2L);
    assertThat(result.leadsByStage()).containsEntry("New", 1L);
    assertThat(result.leadsByStage()).containsEntry("Contacted", 1L);
    assertThat(result.leadsBySource()).containsEntry("Website", 2L);
  }

  @Test
  @DisplayName("Should get dashboard stats with date filter")
  void shouldGetDashboardStatsWithDateFilter() {
    // Arrange
    when(properties.getServices()).thenReturn(services);
    when(services.getLeadServiceUrl()).thenReturn("http://localhost:8081");

    LocalDate startDate = LocalDate.now().minusDays(7);
    LocalDate endDate = LocalDate.now();

    List<PipelineItem> items = createTestItems();
    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    setupWebClientMock(new HashMap<>());

    // Act
    DashboardDtos.DashboardStatsResponse result = service.getDashboardStats(startDate, endDate);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.totalLeads()).isGreaterThanOrEqualTo(0L);
  }

  @Test
  @DisplayName("Should handle WebClient failure gracefully")
  void shouldHandleWebClientFailureGracefully() {
    // Arrange
    when(properties.getServices()).thenReturn(services);
    when(services.getLeadServiceUrl()).thenReturn("http://localhost:8081");

    List<PipelineItem> items = createTestItems();
    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
        .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

    // Act
    DashboardDtos.DashboardStatsResponse result = service.getDashboardStats(null, null);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.leadsBySource()).isEmpty();
    assertThat(result.totalLeads()).isEqualTo(4L);
  }

  @Test
  @DisplayName("Should get conversion metrics")
  void shouldGetConversionMetrics() {
    // Arrange
    List<PipelineItem> items = createTestItemsWithConversion();
    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    // Act
    DashboardDtos.ConversionMetricsResponse result = service.getConversionMetrics(null, null);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.conversionRate()).isGreaterThan(0.0);
    assertThat(result.averageTimeToConvert()).isGreaterThanOrEqualTo(0.0);
    assertThat(result.stageVelocity()).isNotNull();
  }

  @Test
  @DisplayName("Should calculate conversion rate correctly")
  void shouldCalculateConversionRateCorrectly() {
    // Arrange
    List<PipelineItem> items = List.of(
        createItem(1L, wonStage.getId(), LocalDateTime.now().minusDays(5), LocalDateTime.now()),
        createItem(2L, newStage.getId(), LocalDateTime.now().minusDays(3), null),
        createItem(3L, contactedStage.getId(), LocalDateTime.now().minusDays(2), null),
        createItem(4L, lostStage.getId(), LocalDateTime.now().minusDays(1), null)
    );

    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    // Act
    DashboardDtos.ConversionMetricsResponse result = service.getConversionMetrics(null, null);

    // Assert
    assertThat(result.conversionRate()).isEqualTo(25.0); // 1 won out of 4 total = 25%
  }

  @Test
  @DisplayName("Should handle zero leads gracefully")
  void shouldHandleZeroLeadsGracefully() {
    // Arrange
    when(properties.getServices()).thenReturn(services);
    when(services.getLeadServiceUrl()).thenReturn("http://localhost:8081");

    when(pipelineItemRepository.findAll()).thenReturn(List.of());
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage, lostStage));

    setupWebClientMock(new HashMap<>());

    // Act
    DashboardDtos.DashboardStatsResponse result = service.getDashboardStats(null, null);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.totalLeads()).isEqualTo(0L);
    assertThat(result.wonLeads()).isEqualTo(0L);
    assertThat(result.activeLeads()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Should calculate stage velocity")
  void shouldCalculateStageVelocity() {
    // Arrange
    List<PipelineItem> items = List.of(
        createItemWithDaysInStage(1L, newStage.getId(), 5),
        createItemWithDaysInStage(2L, newStage.getId(), 3),
        createItemWithDaysInStage(3L, contactedStage.getId(), 10)
    );

    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage));

    // Act
    DashboardDtos.ConversionMetricsResponse result = service.getConversionMetrics(null, null);

    // Assert
    assertThat(result.stageVelocity()).isNotNull();
    assertThat(result.stageVelocity().get("New")).isEqualTo(4.0); // Average of 5 and 3
    assertThat(result.stageVelocity().get("Contacted")).isEqualTo(10.0);
  }

  @Test
  @DisplayName("Should handle null converted_at timestamp")
  void shouldHandleNullConvertedAt() {
    // Arrange
    List<PipelineItem> items = List.of(
        createItem(1L, wonStage.getId(), LocalDateTime.now().minusDays(5), null)
    );

    when(pipelineItemRepository.findAll()).thenReturn(items);
    when(stageRepository.findAllByOrderByDisplayOrderAsc())
        .thenReturn(List.of(newStage, contactedStage, wonStage));

    // Act
    DashboardDtos.ConversionMetricsResponse result = service.getConversionMetrics(null, null);

    // Assert
    assertThat(result.averageTimeToConvert()).isEqualTo(0.0);
  }

  // Helper methods

  private List<PipelineItem> createTestItems() {
    return List.of(
        createItem(1L, newStage.getId(), LocalDateTime.now().minusDays(5), null),
        createItem(2L, contactedStage.getId(), LocalDateTime.now().minusDays(3), null),
        createItem(3L, wonStage.getId(), LocalDateTime.now().minusDays(2), LocalDateTime.now()),
        createItem(4L, lostStage.getId(), LocalDateTime.now().minusDays(1), null)
    );
  }

  private List<PipelineItem> createTestItemsWithConversion() {
    return List.of(
        createItem(1L, wonStage.getId(), LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(2)),
        createItem(2L, wonStage.getId(), LocalDateTime.now().minusDays(8), LocalDateTime.now().minusDays(1)),
        createItem(3L, newStage.getId(), LocalDateTime.now().minusDays(3), null)
    );
  }

  private PipelineItem createItem(Long id, Long stageId, LocalDateTime createdAt, LocalDateTime convertedAt) {
    PipelineItem item = new PipelineItem();
    item.setId(id);
    item.setLeadId(id * 100);
    item.setStageId(stageId);
    item.setCreatedAt(createdAt);
    item.setConvertedAt(convertedAt);
    item.setDaysInStage(0);
    return item;
  }

  private PipelineItem createItemWithDaysInStage(Long id, Long stageId, int daysInStage) {
    PipelineItem item = createItem(id, stageId, LocalDateTime.now().minusDays(daysInStage), null);
    item.setDaysInStage(daysInStage);
    return item;
  }

  private void setupWebClientMock(Map<String, Long> leadsBySource) {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
        .thenReturn(Mono.just(leadsBySource));
  }
}
