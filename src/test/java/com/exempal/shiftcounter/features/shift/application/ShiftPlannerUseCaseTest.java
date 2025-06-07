package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftPlannerUseCaseTest {

    private final SettingsPort settings = mock(SettingsPort.class);
    private final ActualDataPort actual = mock(ActualDataPort.class);
    private final ShiftPlannerUseCase useCase = new ShiftPlannerUseCase(settings, actual);

    @Test
    void shouldReturnShiftWithOK_whenActualMeetsPlan() {
        LocalDate today = LocalDate.now();
        when(settings.getHours()).thenReturn(List.of("08:00", "09:00", "10:00"));
        when(settings.getHourlyPlans()).thenReturn(List.of(100));
        when(actual.getHourlyActuals(any())).thenReturn(List.of(100, 100, 100));

        Shift expected = ShiftTestFactory.with(today, 300, 300, "OK");
        Shift actualShift = useCase.buildShift(today);

        assertEquals(expected.planned(), actualShift.planned());
        assertEquals(expected.actual(), actualShift.actual());
        assertEquals(expected.comment(), actualShift.comment());
    }

    @Test
    void shouldReturnShiftWithComment_whenUnderperformed() {
        LocalDate today = LocalDate.now();
        when(settings.getHours()).thenReturn(List.of("08:00", "09:00"));
        when(settings.getHourlyPlans()).thenReturn(List.of(100));
        when(actual.getHourlyActuals(any())).thenReturn(List.of(80, 70));

        Shift expected = ShiftTestFactory.with(today, 200, 150, "Недовыполнение");
        Shift actualShift = useCase.buildShift(today);

        assertEquals(expected.planned(), actualShift.planned());
        assertEquals(expected.actual(), actualShift.actual());
        assertEquals(expected.comment(), actualShift.comment());
    }

    @Test
    void shouldHandleEmptyActualGracefully() {
        LocalDate today = LocalDate.now();
        when(settings.getHours()).thenReturn(List.of("08:00", "09:00"));
        when(settings.getHourlyPlans()).thenReturn(List.of(100));
        when(actual.getHourlyActuals(any())).thenReturn(List.of());

        Shift expected = ShiftTestFactory.with(today, 200, 0, "Недовыполнение");
        Shift actualShift = useCase.buildShift(today);

        assertEquals(expected.planned(), actualShift.planned());
        assertEquals(expected.actual(), actualShift.actual());
        assertEquals(expected.comment(), actualShift.comment());
    }
}
