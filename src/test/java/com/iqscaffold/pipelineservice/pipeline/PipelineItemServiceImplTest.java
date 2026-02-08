package com.iqscaffold.pipelineservice.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.iqscaffold.pipelineservice.config.RabbitMQConfig;
import com.iqscaffold.pipelineservice.event.StageChangeEvent;
import com.iqscaffold.pipelineservice.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("PipelineItemService Unit Tests")
class PipelineItemServiceImplTest {

  @Mock
  private PipelineItemRepository pipelineItemRepository;

  @Mock
  private PipelineStageRepository pipelineStageRepository;

  @Mock
  private RabbitTemplate rabbitTemplate;

  private PipelineItemServiceImpl service;

  private PipelineStage newStage;
  private PipelineStage wonStage;
  private PipelineItem testItem;

  @BeforeEach
  void setUp() {
    service = new PipelineItemServiceImpl(pipelineItemRepository, pipelineStageRepository, rabbitTemplate);

    newStage = new PipelineStage("New", 0);
    newStage.setId(1L);
    newStage.setIsActive(true);
    newStage.setIsFinalStage(false);

    wonStage = new PipelineStage("Won", 4);
    wonStage.setId(5L);
    wonStage.setIsActive(true);
    wonStage.setIsFinalStage(true);

    testItem = new PipelineItem();
    testItem.setId(1L);
    testItem.setLeadId(100L);
    testItem.setStageId(1L);
    testItem.setEnteredStageAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should add lead to pipeline")
  void shouldAddLeadToPipeline() {
    // Arrange
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    PipelineItem result = service.addLeadToPipeline(testItem);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(pipelineItemRepository).save(testItem);
  }

  @Test
  @DisplayName("Should create pipeline item with default stage when no stage provided")
  void shouldCreatePipelineItemWithDefaultStage() {
    // Arrange
    PipelineItem newItem = new PipelineItem();
    newItem.setLeadId(200L);
    newItem.setStageId(null);

    when(pipelineItemRepository.existsByLeadId(200L)).thenReturn(false);
    when(pipelineStageRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(newStage));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(newItem);

    // Act
    PipelineItem result = service.createPipelineItem(newItem);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getStageId()).isEqualTo(1L);
    verify(pipelineItemRepository).save(newItem);
  }

  @Test
  @DisplayName("Should throw exception when creating duplicate pipeline item for lead")
  void shouldThrowExceptionWhenCreatingDuplicateItem() {
    // Arrange
    when(pipelineItemRepository.existsByLeadId(100L)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> service.createPipelineItem(testItem))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Pipeline item already exists for lead ID: 100");

    verify(pipelineItemRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when no stages configured")
  void shouldThrowExceptionWhenNoStagesConfigured() {
    // Arrange
    PipelineItem newItem = new PipelineItem();
    newItem.setLeadId(200L);
    newItem.setStageId(null);

    when(pipelineItemRepository.existsByLeadId(200L)).thenReturn(false);
    when(pipelineStageRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of());

    // Act & Assert
    assertThatThrownBy(() -> service.createPipelineItem(newItem))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No pipeline stages configured");
  }

  @Test
  @DisplayName("Should throw exception when stage not found")
  void shouldThrowExceptionWhenStageNotFound() {
    // Arrange
    testItem.setStageId(999L);
    when(pipelineItemRepository.existsByLeadId(100L)).thenReturn(false);
    when(pipelineStageRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> service.createPipelineItem(testItem))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Pipeline stage not found with ID: 999");
  }

  @Test
  @DisplayName("Should get pipeline item by ID")
  void shouldGetPipelineItemById() {
    // Arrange
    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

    // Act
    Optional<PipelineItem> result = service.getPipelineItemById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should get pipeline item by lead ID")
  void shouldGetPipelineItemByLeadId() {
    // Arrange
    when(pipelineItemRepository.findByLeadId(100L)).thenReturn(Optional.of(testItem));

    // Act
    Optional<PipelineItem> result = service.getPipelineItemByLeadId(100L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getLeadId()).isEqualTo(100L);
  }

  @Test
  @DisplayName("Should get all pipeline items with pagination")
  void shouldGetAllPipelineItems() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<PipelineItem> page = new PageImpl<>(List.of(testItem));
    when(pipelineItemRepository.findAll(pageable)).thenReturn(page);

    // Act
    Page<PipelineItem> result = service.getAllPipelineItems(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should get pipeline items by stage with pagination")
  void shouldGetPipelineItemsByStageWithPagination() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<PipelineItem> page = new PageImpl<>(List.of(testItem));
    when(pipelineItemRepository.findByStageId(1L, pageable)).thenReturn(page);

    // Act
    Page<PipelineItem> result = service.getPipelineItemsByStage(1L, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Should get pipeline items by stage without pagination")
  void shouldGetPipelineItemsByStage() {
    // Arrange
    when(pipelineItemRepository.findByStageId(1L)).thenReturn(List.of(testItem));

    // Act
    List<PipelineItem> result = service.getPipelineItemsByStage(1L);

    // Assert
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should update pipeline item")
  void shouldUpdatePipelineItem() {
    // Arrange
    PipelineItem updates = new PipelineItem();
    updates.setExpectedValue(new java.math.BigDecimal("5000.00"));
    updates.setProbability(new java.math.BigDecimal("75.00"));
    updates.setUpdatedBy("user1");

    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    PipelineItem result = service.updatePipelineItem(1L, updates);

    // Assert
    assertThat(result).isNotNull();
    verify(pipelineItemRepository).save(testItem);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent item")
  void shouldThrowExceptionWhenUpdatingNonExistentItem() {
    // Arrange
    when(pipelineItemRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.updatePipelineItem(999L, testItem))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should move item to new stage")
  void shouldMoveToStage() {
    // Arrange
    PipelineStage targetStage = new PipelineStage("Contacted", 1);
    targetStage.setId(2L);

    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineStageRepository.findById(2L)).thenReturn(Optional.of(targetStage));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    PipelineItem result = service.moveToStage(1L, 2L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getStageId()).isEqualTo(2L);
    assertThat(result.getDaysInStage()).isEqualTo(0);
    verify(pipelineItemRepository).save(testItem);
  }

  @Test
  @DisplayName("Should set converted_at when moving to Won stage")
  void shouldSetConvertedAtWhenMovingToWonStage() {
    // Arrange
    testItem.setConvertedAt(null);
    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineStageRepository.findById(5L)).thenReturn(Optional.of(wonStage));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    service.moveToStage(1L, 5L);

    // Assert
    assertThat(testItem.getConvertedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should clear converted_at when moving away from Won stage")
  void shouldClearConvertedAtWhenMovingAwayFromWonStage() {
    // Arrange
    testItem.setStageId(5L);
    testItem.setConvertedAt(LocalDateTime.now());

    PipelineStage contactedStage = new PipelineStage("Contacted", 1);
    contactedStage.setId(2L);
    contactedStage.setIsFinalStage(false);

    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineStageRepository.findById(2L)).thenReturn(Optional.of(contactedStage));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    service.moveToStage(1L, 2L);

    // Assert
    assertThat(testItem.getConvertedAt()).isNull();
  }

  @Test
  @DisplayName("Should publish stage change event when moving to new stage")
  void shouldPublishStageChangeEvent() {
    // Arrange
    PipelineStage targetStage = new PipelineStage("Contacted", 1);
    targetStage.setId(2L);

    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineStageRepository.findById(2L)).thenReturn(Optional.of(targetStage));
    when(pipelineItemRepository.save(any(PipelineItem.class))).thenReturn(testItem);

    // Act
    service.moveToStage(1L, 2L);

    // Assert
    ArgumentCaptor<StageChangeEvent> eventCaptor = ArgumentCaptor.forClass(StageChangeEvent.class);
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.EXCHANGE_NAME),
        eq(RabbitMQConfig.STAGE_CHANGED_ROUTING_KEY),
        eventCaptor.capture()
    );

    StageChangeEvent event = eventCaptor.getValue();
    assertThat(event.getLeadId()).isEqualTo(100L);
    assertThat(event.getOldStageId()).isEqualTo(1L);
    assertThat(event.getNewStageId()).isEqualTo(2L);
  }

  @Test
  @DisplayName("Should throw exception when moving non-existent item")
  void shouldThrowExceptionWhenMovingNonExistentItem() {
    // Arrange
    when(pipelineItemRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.moveToStage(999L, 2L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should throw exception when moving to non-existent stage")
  void shouldThrowExceptionWhenMovingToNonExistentStage() {
    // Arrange
    when(pipelineItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(pipelineStageRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.moveToStage(1L, 999L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should remove item from pipeline")
  void shouldRemoveFromPipeline() {
    // Arrange
    when(pipelineItemRepository.existsById(1L)).thenReturn(true);

    // Act
    service.removeFromPipeline(1L);

    // Assert
    verify(pipelineItemRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw exception when removing non-existent item")
  void shouldThrowExceptionWhenRemovingNonExistentItem() {
    // Arrange
    when(pipelineItemRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> service.removeFromPipeline(999L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should count items by stage")
  void shouldCountByStage() {
    // Arrange
    when(pipelineItemRepository.countByStageId(1L)).thenReturn(5L);

    // Act
    long count = service.countByStage(1L);

    // Assert
    assertThat(count).isEqualTo(5L);
  }

  @Test
  @DisplayName("Should check if item exists by lead ID")
  void shouldCheckExistsByLeadId() {
    // Arrange
    when(pipelineItemRepository.existsByLeadId(100L)).thenReturn(true);

    // Act
    boolean exists = service.existsByLeadId(100L);

    // Assert
    assertThat(exists).isTrue();
  }
}
