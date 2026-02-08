package com.iqscaffold.pipelineservice.followup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.iqscaffold.pipelineservice.event.FollowUpEventPublisher;
import com.iqscaffold.pipelineservice.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpService Unit Tests")
class FollowUpServiceImplTest {

  @Mock
  private FollowUpRepository followUpRepository;

  @Mock
  private FollowUpEventPublisher followUpEventPublisher;

  private FollowUpServiceImpl service;

  private FollowUp testFollowUp;

  @BeforeEach
  void setUp() {
    service = new FollowUpServiceImpl(followUpRepository, followUpEventPublisher);

    testFollowUp = new FollowUp();
    testFollowUp.setId(1L);
    testFollowUp.setLeadId(100L);
    testFollowUp.setTitle("Follow up call");
    testFollowUp.setDescription("Call to discuss proposal");
    testFollowUp.setDueDate(LocalDateTime.now().plusDays(1));
    testFollowUp.setStatus(FollowUpStatus.PENDING);
    testFollowUp.setPriority(FollowUpPriority.HIGH);
  }

  @Test
  @DisplayName("Should schedule follow-up and publish event")
  void shouldScheduleFollowUp() {
    // Arrange
    when(followUpRepository.save(any(FollowUp.class))).thenReturn(testFollowUp);

    // Act
    FollowUp result = service.scheduleFollowUp(testFollowUp);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(followUpRepository).save(testFollowUp);
    verify(followUpEventPublisher).publishFollowUpScheduled(testFollowUp);
  }

  @Test
  @DisplayName("Should schedule follow-up without event publisher")
  void shouldScheduleFollowUpWithoutEventPublisher() {
    // Arrange
    FollowUpServiceImpl serviceWithoutPublisher = new FollowUpServiceImpl(followUpRepository, null);
    when(followUpRepository.save(any(FollowUp.class))).thenReturn(testFollowUp);

    // Act
    FollowUp result = serviceWithoutPublisher.scheduleFollowUp(testFollowUp);

    // Assert
    assertThat(result).isNotNull();
    verify(followUpRepository).save(testFollowUp);
  }

  @Test
  @DisplayName("Should get follow-up by ID")
  void shouldGetFollowUpById() {
    // Arrange
    when(followUpRepository.findById(1L)).thenReturn(Optional.of(testFollowUp));

    // Act
    Optional<FollowUp> result = service.getFollowUpById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should get all follow-ups with pagination")
  void shouldGetAllFollowUps() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<FollowUp> page = new PageImpl<>(List.of(testFollowUp));
    when(followUpRepository.findAll(pageable)).thenReturn(page);

    // Act
    Page<FollowUp> result = service.getAllFollowUps(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should get follow-ups by lead ID with pagination")
  void shouldGetFollowUpsByLeadIdWithPagination() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<FollowUp> page = new PageImpl<>(List.of(testFollowUp));
    when(followUpRepository.findByLeadId(100L, pageable)).thenReturn(page);

    // Act
    Page<FollowUp> result = service.getFollowUpsByLeadId(100L, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getLeadId()).isEqualTo(100L);
  }

  @Test
  @DisplayName("Should get follow-ups by lead ID without pagination")
  void shouldGetFollowUpsByLeadId() {
    // Arrange
    when(followUpRepository.findByLeadId(100L)).thenReturn(List.of(testFollowUp));

    // Act
    List<FollowUp> result = service.getFollowUpsByLeadId(100L);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLeadId()).isEqualTo(100L);
  }

  @Test
  @DisplayName("Should get follow-ups by status")
  void shouldGetFollowUpsByStatus() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<FollowUp> page = new PageImpl<>(List.of(testFollowUp));
    when(followUpRepository.findByStatus(FollowUpStatus.PENDING, pageable)).thenReturn(page);

    // Act
    Page<FollowUp> result = service.getFollowUpsByStatus(FollowUpStatus.PENDING, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo(FollowUpStatus.PENDING);
  }

  @Test
  @DisplayName("Should get today's follow-ups")
  void shouldGetTodayFollowUps() {
    // Arrange
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

    when(followUpRepository.findByStatusAndDueDateBetween(
        FollowUpStatus.PENDING, startOfDay, endOfDay
    )).thenReturn(List.of(testFollowUp));

    // Act
    List<FollowUp> result = service.getTodayFollowUps();

    // Assert
    assertThat(result).hasSize(1);
    verify(followUpRepository).findByStatusAndDueDateBetween(
        FollowUpStatus.PENDING, startOfDay, endOfDay
    );
  }

  @Test
  @DisplayName("Should get overdue follow-ups")
  void shouldGetOverdueFollowUps() {
    // Arrange
    when(followUpRepository.findOverdueFollowUps(
        any(FollowUpStatus.class), any(LocalDateTime.class)
    )).thenReturn(List.of(testFollowUp));

    // Act
    List<FollowUp> result = service.getOverdueFollowUps();

    // Assert
    assertThat(result).hasSize(1);
    verify(followUpRepository).findOverdueFollowUps(
        any(FollowUpStatus.class), any(LocalDateTime.class)
    );
  }

  @Test
  @DisplayName("Should update follow-up")
  void shouldUpdateFollowUp() {
    // Arrange
    FollowUp updates = new FollowUp();
    updates.setTitle("Updated title");
    updates.setDescription("Updated description");
    updates.setDueDate(LocalDateTime.now().plusDays(2));
    updates.setPriority(FollowUpPriority.MEDIUM);
    updates.setAssignedTo("user2");
    updates.setUpdatedBy("user1");

    when(followUpRepository.findById(1L)).thenReturn(Optional.of(testFollowUp));
    when(followUpRepository.save(any(FollowUp.class))).thenReturn(testFollowUp);

    // Act
    FollowUp result = service.updateFollowUp(1L, updates);

    // Assert
    assertThat(result).isNotNull();
    assertThat(testFollowUp.getTitle()).isEqualTo("Updated title");
    assertThat(testFollowUp.getDescription()).isEqualTo("Updated description");
    verify(followUpRepository).save(testFollowUp);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent follow-up")
  void shouldThrowExceptionWhenUpdatingNonExistentFollowUp() {
    // Arrange
    when(followUpRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.updateFollowUp(999L, testFollowUp))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should complete follow-up and publish event")
  void shouldCompleteFollowUp() {
    // Arrange
    when(followUpRepository.findById(1L)).thenReturn(Optional.of(testFollowUp));
    when(followUpRepository.save(any(FollowUp.class))).thenReturn(testFollowUp);

    // Act
    FollowUp result = service.completeFollowUp(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(testFollowUp.getStatus()).isEqualTo(FollowUpStatus.COMPLETED);
    assertThat(testFollowUp.getCompletedAt()).isNotNull();
    verify(followUpRepository).save(testFollowUp);
    verify(followUpEventPublisher).publishFollowUpCompleted(testFollowUp);
  }

  @Test
  @DisplayName("Should complete follow-up without event publisher")
  void shouldCompleteFollowUpWithoutEventPublisher() {
    // Arrange
    FollowUpServiceImpl serviceWithoutPublisher = new FollowUpServiceImpl(followUpRepository, null);
    when(followUpRepository.findById(1L)).thenReturn(Optional.of(testFollowUp));
    when(followUpRepository.save(any(FollowUp.class))).thenReturn(testFollowUp);

    // Act
    FollowUp result = serviceWithoutPublisher.completeFollowUp(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(testFollowUp.getStatus()).isEqualTo(FollowUpStatus.COMPLETED);
    verify(followUpRepository).save(testFollowUp);
  }

  @Test
  @DisplayName("Should throw exception when completing non-existent follow-up")
  void shouldThrowExceptionWhenCompletingNonExistentFollowUp() {
    // Arrange
    when(followUpRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.completeFollowUp(999L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should delete follow-up")
  void shouldDeleteFollowUp() {
    // Arrange
    when(followUpRepository.existsById(1L)).thenReturn(true);

    // Act
    service.deleteFollowUp(1L);

    // Assert
    verify(followUpRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent follow-up")
  void shouldThrowExceptionWhenDeletingNonExistentFollowUp() {
    // Arrange
    when(followUpRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> service.deleteFollowUp(999L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(followUpRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should count today's follow-ups")
  void shouldCountTodayFollowUps() {
    // Arrange
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

    when(followUpRepository.countByStatusAndDueDateBetween(
        FollowUpStatus.PENDING, startOfDay, endOfDay
    )).thenReturn(5L);

    // Act
    long count = service.countTodayFollowUps();

    // Assert
    assertThat(count).isEqualTo(5L);
  }
}
