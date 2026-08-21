package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.application.calculator.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class StoppageReconcilesServiceTest {
    private final ReconcileSignalQueryPort signals = mock(ReconcileSignalQueryPort.class);
    private final ReconcileShiftRepository shifts = mock(ReconcileShiftRepository.class);
    private final StoppageRepository stoppages = mock(StoppageRepository.class);
    private StoppageReconcilesService service;
    private Shift shift;
    private LocalDate date;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        reset(signals, shifts, stoppages);
        date = LocalDate.of(2026, 8, 7);
        end = LocalDateTime.of(date, java.time.LocalTime.of(9, 0));
        shift = new Shift(1L, date, "sensor-1", List.of(100), 20, List.of(20), List.of("08:00"));
        when(shifts.findForUpdateByDateAndSensorId(date, "sensor-1")).thenReturn(Optional.of(shift));
        when(signals.findTimestamps(eq("sensor-1"), any(), any())).thenReturn(List.of());
        StoppageCalculator calculator = new StoppageCalculatorImpl(
                new StoppageFixedLossCalculator(new StoppageDetector(), Duration.ofHours(2)),
                new StoppageTempoLossCalculator());
        service = new StoppageReconcilesService(new ShiftIntervalService(), signals, calculator,
                new StoppageMatcher(), shifts, stoppages);
    }

    @Test
    void createsOneResidualTempoThroughAggregateWriteBoundary() {
        when(signals.findTimestamps(eq("sensor-1"), any(), any()))
                .thenReturn(List.of(end.minusMinutes(30)));
        when(stoppages.findActiveByShiftSensorAndIntervalRange(1L, Stoppage.PRIMARY_SENSOR, 0, 1))
                .thenReturn(List.of());
        when(stoppages.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ReconcileResult result = service.reconcile(command());

        assertThat(result.persisted()).isTrue();
        assertThat(result.activeStoppages()).singleElement().satisfies(value -> {
            assertThat(value.detectionType()).isEqualTo(DetectionType.TEMPO);
            assertThat(value.lostCans()).isEqualTo(80);
        });
        verify(stoppages).saveAll(anyList());
    }

    @Test
    void repeatedIdenticalReconcileDoesNotWriteOrChangeIdentity() {
        when(signals.findTimestamps(eq("sensor-1"), any(), any()))
                .thenReturn(List.of(end.minusMinutes(30)));
        Stoppage existing = new Stoppage(10L, UUID.randomUUID(), 1L, Stoppage.PRIMARY_SENSOR, 0,
                LocalDateTime.of(date, java.time.LocalTime.of(8, 0)), Duration.ofMinutes(48), 48, 80,
                DetectionType.TEMPO, StoppageState.ACTIVE, List.of(), 3L);
        when(stoppages.findActiveByShiftSensorAndIntervalRange(1L, Stoppage.PRIMARY_SENSOR, 0, 1))
                .thenReturn(List.of(existing));

        ReconcileResult result = service.reconcile(command());

        assertThat(result.changedRows()).isZero();
        assertThat(result.activeStoppages()).singleElement()
                .extracting(Stoppage::detectionKey).isEqualTo(existing.detectionKey());
        verify(stoppages, never()).saveAll(anyList());
    }

    @Test
    void doesNotCreateStoppagesWhenSensorHasNoSignalsForInterval() {
        ReconcileResult result = service.reconcile(command());

        assertThat(result.persisted()).isTrue();
        assertThat(result.activeStoppages()).isEmpty();
        assertThat(result.changedRows()).isZero();
        verify(stoppages, never()).saveAll(anyList());
        verify(stoppages, never()).findActiveByShiftSensorAndIntervalRange(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void usesAbsoluteCrossMidnightBoundariesForSignalQuery() {
        shift = new Shift(1L, date, "sensor-1", List.of(100, 100), 200,
                List.of(100, 100), List.of("23:30", "00:30"));
        when(shifts.findForUpdateByDateAndSensorId(date, "sensor-1")).thenReturn(Optional.of(shift));
        when(stoppages.findActiveByShiftSensorAndIntervalRange(1L, Stoppage.PRIMARY_SENSOR, 0, 2))
                .thenReturn(List.of());

        service.reconcile(new ReconcileStoppagesCommand(date, Stoppage.PRIMARY_SENSOR, 1,
                LocalDateTime.of(2026, 8, 8, 1, 0)));

        verify(signals).findTimestamps("sensor-1", LocalDateTime.of(2026, 8, 8, 0, 30),
                LocalDateTime.of(2026, 8, 8, 1, 0));
    }

    @Test
    void resolveOnlyCommandResolvesRemovedIntervalWithoutCalculation() {
        Stoppage existing = new Stoppage(10L, UUID.randomUUID(), 1L, Stoppage.PRIMARY_SENSOR, 4,
                LocalDateTime.of(date, java.time.LocalTime.of(12, 0)), Duration.ofMinutes(10),
                10, 10, DetectionType.FIXED, StoppageState.ACTIVE, List.of(), 0L);
        when(stoppages.findActiveByShiftSensorAndIntervalRange(1L, Stoppage.PRIMARY_SENSOR, 4, 4))
                .thenReturn(List.of(existing));
        when(stoppages.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ReconcileResult result = service.reconcile(ReconcileStoppagesCommand.resolveRemovedInterval(
                date, Stoppage.PRIMARY_SENSOR, 4, end));

        assertThat(result.changedRows()).isEqualTo(1);
        verify(stoppages).saveAll(argThat(values -> values.getFirst().state() == StoppageState.RESOLVED));
        verifyNoInteractions(signals);
    }

    @Test
    void sensorFiveHasNoOwnStoppageReconcileWorkflow() {
        ReconcileResult result = service.reconcile(new ReconcileStoppagesCommand(
                date, "sensor-5", 0, end));

        assertThat(result.persisted()).isTrue();
        assertThat(result.activeStoppages()).isEmpty();
        verifyNoInteractions(shifts, signals, stoppages);
    }

    private ReconcileStoppagesCommand command() {
        return new ReconcileStoppagesCommand(date, Stoppage.PRIMARY_SENSOR, 0, end);
    }
}
