package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LossExplanationServiceTest {
    private StoppageRepository stoppages;
    private LossExplanationService service;
    private ActualDataPort shifts;
    private EventPublisherPort events;
    private Stoppage stoppage;
    private CurrentCommentActor currentActor;
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID OTHER_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final Instant NOW = Instant.parse("2026-08-07T08:00:00Z");

    @BeforeEach
    void setUp() {
        stoppages = mock(StoppageRepository.class);
        shifts = mock(ActualDataPort.class);
        events = mock(EventPublisherPort.class);
        Shift shift = mock(Shift.class);
        when(shift.getDate()).thenReturn(java.time.LocalDate.of(2026, 8, 7));
        when(shifts.findById(1L)).thenReturn(Optional.of(shift));
        currentActor = mock(CurrentCommentActor.class);
        when(currentActor.require()).thenReturn(new CommentActor(USER_ID, "Alex", com.exempal.shiftcounter.features.user.domain.UserRole.USER));
        service = new LossExplanationService(stoppages, shifts, events, currentActor,
                Clock.fixed(NOW, ZoneOffset.UTC));
        stoppage = stoppage(List.of());
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage));
        when(stoppages.findById(10L)).thenReturn(Optional.of(stoppage));
        when(stoppages.save(any())).thenAnswer(invocation -> persisted(invocation.getArgument(0)));
    }

    @Test
    void createsExplanationWithoutAcceptingSystemOwnedFields() {
        LossExplanation saved = service.create(10L, LossCategory.MATERIAL, "Roll change", 4);
        assertThat(saved.stoppageId()).isEqualTo(10L);
        assertThat(saved.allocatedMinutes()).isEqualTo(4);
        assertThat(saved.allocatedCans()).isEqualTo(40);
        assertThat(saved.authorUserId()).isEqualTo(USER_ID);
        assertThat(saved.authorDisplayName()).isEqualTo("Alex");
        assertThat(saved.createdAt()).isEqualTo(NOW);
        verify(events).publish(new CommentsUpdatedEvent(java.time.LocalDate.of(2026, 8, 7), "sensor-1"));
    }

    @Test
    void rejectsAggregateOverAllocation() {
        stoppage = stoppage(List.of(new LossExplanation(2L, 10L, LossCategory.QUALITY, "Existing", 7, 70)));
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage));
        assertThatThrownBy(() -> service.create(10L, LossCategory.MATERIAL, "Too much", 4))
                .isInstanceOf(LossAllocationException.class).hasMessageContaining("exceed");
        verify(stoppages, never()).save(any());
    }

    @Test
    void rejectsExplanationForLegacyOrMissingRow() {
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(10L, LossCategory.BREAKDOWN, "Legacy", 1))
                .isInstanceOf(LossExplanationNotFoundException.class);
    }

    @Test
    void updateCannotMoveExplanationFromAnotherLoss() {
        assertThatThrownBy(() -> service.update(10L, 3L, LossCategory.MATERIAL, "Move", 1))
                .isInstanceOf(LossExplanationNotFoundException.class);
    }

    @Test
    void userCannotEditOrDeleteAnotherUsersComment() {
        LossExplanation other = authored(2L, OTHER_ID, "Maria");
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage(List.of(other))));
        assertThatThrownBy(() -> service.update(10L, 2L, LossCategory.QUALITY, "changed", 2))
                .isInstanceOf(CommentAccessDeniedException.class);
        assertThatThrownBy(() -> service.delete(10L, 2L)).isInstanceOf(CommentAccessDeniedException.class);
        verify(stoppages, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "OWNER"})
    void privilegedUserMayEditAnyCommentWithoutReplacingAuthor(String role) {
        LossExplanation other = authored(2L, OTHER_ID, "Maria");
        when(currentActor.require()).thenReturn(new CommentActor(USER_ID, "Alex",
                com.exempal.shiftcounter.features.user.domain.UserRole.valueOf(role)));
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage(List.of(other))));
        LossExplanation saved = service.update(10L, 2L, LossCategory.QUALITY, "fixed", 2);
        assertThat(saved.authorUserId()).isEqualTo(OTHER_ID);
        assertThat(saved.authorDisplayName()).isEqualTo("Maria");
        assertThat(saved.lastModifiedBy()).isEqualTo(USER_ID);
        assertThat(saved.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void sensorFiveCannotOwnLossExplanations() {
        stoppage = stoppage("sensor-5", List.of());
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage));

        assertThatThrownBy(() -> service.create(10L, LossCategory.MATERIAL, "Not owned", 1))
                .isInstanceOf(LossAllocationException.class)
                .hasMessageContaining("Sensor 5");
        verify(stoppages, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-6"})
    void editableSensorsOwnIndependentExplanationCrud(String sensorId) {
        stoppage = stoppage(sensorId, List.of());
        when(stoppages.findForUpdateById(10L)).thenReturn(Optional.of(stoppage));

        LossExplanation saved = service.create(10L, LossCategory.QUALITY, sensorId, 2);

        assertThat(saved.comment()).isEqualTo(sensorId);
        verify(events).publish(new CommentsUpdatedEvent(java.time.LocalDate.of(2026, 8, 7), sensorId));
    }

    private Stoppage stoppage(List<LossExplanation> explanations) {
        return stoppage(Stoppage.PRIMARY_SENSOR, explanations);
    }

    private Stoppage stoppage(String sensorId, List<LossExplanation> explanations) {
        return new Stoppage(10L, UUID.fromString("00000000-0000-0000-0000-000000000010"), 1L,
                sensorId, 0, LocalDateTime.of(2026, 8, 7, 8, 0), Duration.ofMinutes(10),
                10, 100, DetectionType.FIXED, StoppageState.ACTIVE, explanations, 0L);
    }

    private Stoppage persisted(Stoppage source) {
        List<LossExplanation> values = source.explanations().stream()
                .map(value -> value.id() == null
                        ? new LossExplanation(1L, 10L, value.category(), value.comment(),
                        value.allocatedMinutes(), value.allocatedCans(), value.authorUserId(),
                        value.authorDisplayName(), value.createdAt(), value.updatedAt(),
                        value.lastModifiedBy(), 0L) : value)
                .toList();
        return new Stoppage(source.id(), source.detectionKey(), source.shiftId(), source.sensorKey(),
                source.intervalIndex(), source.startedAt(), source.exactDuration(), source.roundedMinutes(),
                source.lostCans(), source.detectionType(), source.state(), values, source.version());
    }


    private LossExplanation authored(long id, UUID authorId, String name) {
        return new LossExplanation(id, 10L, LossCategory.QUALITY, "Existing", 2, 20,
                authorId, name, NOW.minusSeconds(60), NOW.minusSeconds(60), null, 0L);
    }
}
