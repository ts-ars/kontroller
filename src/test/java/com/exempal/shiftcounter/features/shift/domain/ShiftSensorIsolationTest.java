package com.exempal.shiftcounter.features.shift.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftSensorIsolationTest {
    @Test
    void incrementChangesOnlySelectedSensorShift() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift first = new Shift(date, "sensor-1", List.of(10), 0, List.of(0), List.of("08:00"));
        Shift second = new Shift(date, "sensor-2", List.of(10), 0, List.of(0), List.of("08:00"));

        Shift incremented = second.withIncrementedHourlyActualValue(0);

        assertThat(first.getHourlyActualValues()).containsExactly(0);
        assertThat(incremented.getSensorId()).isEqualTo("sensor-2");
        assertThat(incremented.getHourlyActualValues()).containsExactly(1);
    }
}
