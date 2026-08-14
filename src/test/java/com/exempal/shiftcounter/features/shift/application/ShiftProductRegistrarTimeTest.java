package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShiftProductRegistrarTimeTest {
    private final ShiftPlannerUseCase planner = mock(ShiftPlannerUseCase.class);
    private final ShiftExtenderService extender = mock(ShiftExtenderService.class);
    private final ShiftReconcilePort reconcile = mock(ShiftReconcilePort.class);
    private final ProductionDayService productionDays = new ProductionDayService(
            Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC));
    private final ShiftProductRegistrar registrar = new ShiftProductRegistrar(planner, extender,
            productionDays, new ShiftIntervalService(), reconcile);

    @BeforeEach
    void resetMocks() {
        reset(planner, extender, reconcile);
    }

    @Test
    void afterMidnightSignalUsesPreviousProductionDateAndDoesNotFinalizeOpenInterval() {
        LocalDate productionDate = LocalDate.of(2026, 8, 9);
        Shift shift = shift(productionDate, List.of(10, 10, 10), List.of(0, 0, 0),
                List.of("23:00", "00:00", "06:00"));
        when(planner.getOrCreateShift(productionDate, "sensor-1")).thenReturn(shift);
        when(extender.extendIfNeeded(any(), same(shift))).thenReturn(shift);

        registrar.registerProduct(LocalDateTime.of(2026, 8, 10, 0, 15));

        ArgumentCaptor<Shift> saved = ArgumentCaptor.forClass(Shift.class);
        verify(planner).updateShift(saved.capture());
        assertThat(saved.getValue().getHourlyActualValues()).containsExactly(0, 1, 0);
        verifyNoInteractions(reconcile);
    }

    @Test
    void planRequiredIntervalAccumulatesActualWithoutReconcile() {
        LocalDate productionDate = LocalDate.of(2026, 8, 10);
        Shift shift = shift(productionDate, List.of(10), List.of(0, 0), List.of("15:30", "16:00"));
        when(planner.getOrCreateShift(productionDate, "sensor-1")).thenReturn(shift);
        when(extender.extendIfNeeded(any(), same(shift))).thenReturn(shift);

        registrar.registerProduct(LocalDateTime.of(2026, 8, 10, 16, 15));

        ArgumentCaptor<Shift> saved = ArgumentCaptor.forClass(Shift.class);
        verify(planner).updateShift(saved.capture());
        assertThat(saved.getValue().getHourlyActualValues()).containsExactly(0, 1);
        verifyNoInteractions(reconcile);
    }

    private Shift shift(LocalDate date, List<Integer> plans, List<Integer> actuals, List<String> labels) {
        return new Shift(1L, date, plans, actuals.stream().mapToInt(Integer::intValue).sum(), actuals, labels);
    }
}
