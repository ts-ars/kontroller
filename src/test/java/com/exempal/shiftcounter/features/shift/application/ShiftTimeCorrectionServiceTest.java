package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class ShiftTimeCorrectionServiceTest {
    @Test
    void redistributesSavedSignalsByTimestampAndReconcilesPlannedIntervals() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift current = new Shift(1L, date, List.of(10, 10), 2,
                List.of(1, 1), List.of("08:00", "09:00"));
        ShiftSignalHistoryPort signals = mock(ShiftSignalHistoryPort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ShiftReconcilePort reconcile = mock(ShiftReconcilePort.class);
        when(signals.findTimestamps(eq("sensor-1"), any(), any())).thenReturn(List.of(
                LocalDateTime.of(2026, 8, 10, 8, 15),
                LocalDateTime.of(2026, 8, 10, 9, 15)));
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        Shift updated = service.apply(current, List.of("08:00", "08:30", "09:00"),
                List.of(5, 5, 10), true, LocalDateTime.of(2026, 8, 10, 10, 0));

        assertThat(updated.getHourlyActualValues()).containsExactly(1, 0, 1);
        assertThat(updated.getActual()).isEqualTo(2);
        verify(reconcile, times(3)).reconcile(eq(date), eq("sensor-1"), anyInt(), any());
    }

    @Test
    void removedIntervalsAreResolvedThroughUnifiedReconcileUseCase() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift current = new Shift(1L, date, List.of(10, 10, 10), 0,
                List.of(0, 0, 0), List.of("08:00", "09:00", "10:00"));
        ShiftSignalHistoryPort signals = mock(ShiftSignalHistoryPort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ShiftReconcilePort reconcile = mock(ShiftReconcilePort.class);
        when(signals.findTimestamps(eq("sensor-1"), any(), any())).thenReturn(List.of());
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        service.apply(current, List.of("08:00", "09:00"), List.of(10, 10), true,
                LocalDateTime.of(2026, 8, 10, 10, 0));

        verify(reconcile, times(2)).reconcile(eq(date), eq("sensor-1"), anyInt(), any());
        verify(reconcile).resolveRemovedInterval(date, "sensor-1", 2,
                LocalDateTime.of(2026, 8, 10, 10, 0));
    }

    @Test
    void intervalThatRemainsAsPlanRequiredResolvesItsOldPlannedStoppage() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift current = new Shift(1L, date, List.of(10, 10, 10), 1,
                List.of(0, 0, 1), List.of("08:00", "09:00", "10:00"));
        ShiftSignalHistoryPort signals = mock(ShiftSignalHistoryPort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ShiftReconcilePort reconcile = mock(ShiftReconcilePort.class);
        when(signals.findTimestamps(eq("sensor-1"), any(), any())).thenReturn(List.of(
                LocalDateTime.of(2026, 8, 10, 10, 15)));
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        service.apply(current, List.of("08:00", "09:00"), List.of(10, 10), true,
                LocalDateTime.of(2026, 8, 10, 10, 30));

        verify(reconcile, times(2)).reconcile(eq(date), eq("sensor-1"), anyInt(), any());
        verify(reconcile).resolveRemovedInterval(date, "sensor-1", 2,
                LocalDateTime.of(2026, 8, 10, 10, 30));
    }
}
