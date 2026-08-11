package com.exempal.shiftcounter.features.shift.application.projection;

import com.exempal.shiftcounter.features.shift.application.ShiftSettingsPort;
import com.exempal.shiftcounter.features.shift.application.ShiftSettings;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShiftProjectionUseCaseTest {

    private ShiftSettingsPort settings;
    private ActualDataPort actualDataPort;
    private ShiftExplanationPort explanations;
    private ShiftProjectionUseCase useCase;

    @BeforeEach
    void setUp() {
        settings = mock(ShiftSettingsPort.class);
        actualDataPort = mock(ActualDataPort.class);
        explanations = mock(ShiftExplanationPort.class);
        when(explanations.findByInterval(any(), anyString())).thenReturn(java.util.Map.of());
        useCase = new ShiftProjectionUseCase(settings, actualDataPort, explanations);
    }

    @Test
    void shouldBuildCorrectViewWhenActualExists() {
        LocalDate date = LocalDate.of(2025, 6, 8);
        List<String> hours = List.of("08:00", "09:00", "10:00");
        List<Integer> plan = List.of(100, 100, 100);
        List<Integer> actualValues = List.of(90, 100, 100);
        int totalActual = actualValues.stream().mapToInt(Integer::intValue).sum();

        Shift shift = new Shift(
                1L,
                date,
                plan,
                totalActual,
                actualValues,
                hours
        );

        when(actualDataPort.findByDateAndSensorId(date, "sensor-1")).thenReturn(Optional.of(shift));
        when(settings.getForSensor(anyString())).thenReturn(testSettings(hours, plan));

        ShiftView view = useCase.buildView(date);

        assertThat(view.date()).isEqualTo(date);
        assertThat(view.plan()).isEqualTo(plan);
        assertThat(view.actual()).isEqualTo(actualValues);
        assertThat(view.hours()).isEqualTo(hours);
    }

    @Test
    void shouldBuildCorrectViewWhenNoShiftExists() {
        LocalDate date = LocalDate.of(2025, 6, 8);
        List<String> hours = List.of("08:00", "09:00");
        List<String> planStrings = List.of("100", "100");

        when(actualDataPort.findByDateAndSensorId(date, "sensor-1")).thenReturn(Optional.empty());
        when(settings.getForSensor(anyString())).thenReturn(testSettings(hours, List.of(100, 100)));

        ShiftView view = useCase.buildView(date);

        assertThat(view.date()).isEqualTo(date);
        assertThat(view.plan()).isEqualTo(List.of(100, 100));
        assertThat(view.actual()).isEqualTo(List.of(0, 0));
        assertThat(view.hours()).containsExactlyElementsOf(hours);
    }

    @Test
    void shouldPadShortShiftWithZeros() {
        LocalDate date = LocalDate.of(2025, 7, 6);
        List<String> hours = List.of("08:00", "09:00", "10:00", "11:00");
        List<Integer> shortActual = List.of(10, 20);
        List<Integer> shortPlan = List.of(50);
        int total = shortActual.stream().mapToInt(Integer::intValue).sum();

        Shift shortShift = new Shift(
                1L,
                date,
                shortPlan,
                total,
                shortActual,
                hours
        );

        when(actualDataPort.findByDateAndSensorId(date, "sensor-1")).thenReturn(Optional.of(shortShift));
        when(settings.getForSensor(anyString())).thenReturn(testSettings(hours, List.of(50, 0, 0, 0)));

        ShiftView view = useCase.buildView(date);

        assertThat(view.plan()).isEqualTo(List.of(50, 0, 0, 0));
        assertThat(view.actual()).isEqualTo(List.of(10, 20, 0, 0));
        assertThat(view.hours()).isEqualTo(hours);
    }

    @Test
    void eveningSliceIncludesAfterTwentyThreeAndKeepsMultipleTypedExplanations() {
        LocalDate date = LocalDate.of(2026, 8, 7);
        List<String> hours = List.of("07:00", "15:00", "23:30", "00:30");
        List<Integer> plan = List.of(100, 200, 300, 400);
        Shift shift = new Shift(6L, date, "sensor-6", plan, 10,
                List.of(1, 2, 3, 4), hours);
        when(actualDataPort.findByDateAndSensorId(date, "sensor-6")).thenReturn(Optional.of(shift));
        when(settings.getForSensor("sensor-6")).thenReturn(testSettings(hours, plan));
        when(explanations.findByInterval(date, "sensor-6")).thenReturn(java.util.Map.of(2, List.of(
                new IntervalExplanationView("sensor-6", "First", 3),
                new IntervalExplanationView("sensor-6", "Second", 4))));

        ShiftView view = useCase.buildView(date, "sensor-6", ShiftSlice.EVENING);

        assertThat(view.hours()).containsExactly("15:00", "23:30", "00:30");
        assertThat(view.actual()).containsExactly(2, 3, 4);
        assertThat(view.explanations().get(1)).extracting(IntervalExplanationView::comment)
                .containsExactly("First", "Second");
    }

    private ShiftSettings testSettings(List<String> labels, List<Integer> plans) {
        return new ShiftSettings(labels, plans);
    }
}
