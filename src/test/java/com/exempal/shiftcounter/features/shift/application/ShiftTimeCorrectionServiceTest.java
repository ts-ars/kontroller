package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesUseCase;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
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
        SignalStoragePort signals = mock(SignalStoragePort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ReconcileStoppagesUseCase reconcile = mock(ReconcileStoppagesUseCase.class);
        when(signals.findBySensorAndRange(eq("sensor-1"), any(), any())).thenReturn(List.of(
                new Signal(LocalDateTime.of(2026, 8, 10, 8, 15)),
                new Signal(LocalDateTime.of(2026, 8, 10, 9, 15))));
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        Shift updated = service.apply(current, List.of("08:00", "08:30", "09:00"),
                List.of(5, 5, 10), true, LocalDateTime.of(2026, 8, 10, 10, 0));

        assertThat(updated.getHourlyActualValues()).containsExactly(1, 0, 1);
        assertThat(updated.getActual()).isEqualTo(2);
        verify(reconcile, times(3)).reconcile(any());
    }

    @Test
    void removedIntervalsAreResolvedThroughUnifiedReconcileUseCase() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift current = new Shift(1L, date, List.of(10, 10, 10), 0,
                List.of(0, 0, 0), List.of("08:00", "09:00", "10:00"));
        SignalStoragePort signals = mock(SignalStoragePort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ReconcileStoppagesUseCase reconcile = mock(ReconcileStoppagesUseCase.class);
        when(signals.findBySensorAndRange(eq("sensor-1"), any(), any())).thenReturn(List.of());
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        service.apply(current, List.of("08:00", "09:00"), List.of(10, 10), true,
                LocalDateTime.of(2026, 8, 10, 10, 0));

        ArgumentCaptor<com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand> commands =
                ArgumentCaptor.forClass(com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand.class);
        verify(reconcile, times(3)).reconcile(commands.capture());
        assertThat(commands.getAllValues()).anyMatch(command -> command.resolveOnly()
                && command.intervalIndex() == 2);
    }

    @Test
    void intervalThatRemainsAsPlanRequiredResolvesItsOldPlannedStoppage() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift current = new Shift(1L, date, List.of(10, 10, 10), 1,
                List.of(0, 0, 1), List.of("08:00", "09:00", "10:00"));
        SignalStoragePort signals = mock(SignalStoragePort.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        ReconcileStoppagesUseCase reconcile = mock(ReconcileStoppagesUseCase.class);
        when(signals.findBySensorAndRange(eq("sensor-1"), any(), any())).thenReturn(List.of(
                new Signal(LocalDateTime.of(2026, 8, 10, 10, 15))));
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new ShiftTimeCorrectionService(new ShiftIntervalService(), signals, shifts, reconcile);

        service.apply(current, List.of("08:00", "09:00"), List.of(10, 10), true,
                LocalDateTime.of(2026, 8, 10, 10, 30));

        ArgumentCaptor<com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand> commands =
                ArgumentCaptor.forClass(com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand.class);
        verify(reconcile, times(3)).reconcile(commands.capture());
        assertThat(commands.getAllValues()).anyMatch(command -> command.resolveOnly()
                && command.intervalIndex() == 2);
    }
}
