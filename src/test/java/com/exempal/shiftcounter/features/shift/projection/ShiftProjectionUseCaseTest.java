package com.exempal.shiftcounter.features.shift.projection;

import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.ShiftHour;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShiftProjectionUseCaseTest {

    private ShiftSettingsProvider settings;
    private ActualDataPort actualDataPort;
    private ShiftProjectionUseCase useCase;

    @BeforeEach
    void setUp() {
        settings = mock(ShiftSettingsProvider.class);
        actualDataPort = mock(ActualDataPort.class);
        useCase = new ShiftProjectionUseCase(settings, actualDataPort);
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

    private Settings testSettings(List<String> labels, List<Integer> plans) {
        List<ShiftHour> hours = labels.stream()
                .map(LocalTime::parse)
                .map(start -> new ShiftHour(start, start.plusHours(1)))
                .toList();
        return new Settings(hours, plans);
    }
}
