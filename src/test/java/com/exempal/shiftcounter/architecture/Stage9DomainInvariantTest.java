package com.exempal.shiftcounter.architecture;

import com.exempal.shiftcounter.features.sensor.domain.SensorDefinition;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.settings.domain.IntervalSetting;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Stage9DomainInvariantTest {

    @Test
    void shiftRejectsNegativePlanAndActualValues() {
        assertThatThrownBy(() -> shift(List.of(-1), List.of(0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> shift(List.of(1), List.of(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shiftRejectsPlanLongerThanItsIntervalLabels() {
        assertThatThrownBy(() -> new Shift(LocalDate.of(2026, 8, 10), "sensor-1",
                List.of(1, 2), 0, List.of(0), List.of("07:00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingsGroupRejectsDuplicateStartsAndNonContiguousOrder() {
        assertThatThrownBy(() -> group(
                new IntervalSetting(LocalTime.of(7, 0), 1, 0),
                new IntervalSetting(LocalTime.of(7, 0), 2, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> group(new IntervalSetting(LocalTime.of(7, 0), 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingsGroupRejectsStartsOutsideProductionDayOrder() {
        assertThatThrownBy(() -> group(
                new IntervalSetting(LocalTime.of(8, 0), 1, 0),
                new IntervalSetting(LocalTime.of(7, 30), 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sensorRequiresExactlyOneNonBlankSettingsGroupIdentity() {
        assertThatThrownBy(() -> new SensorDefinition(SensorId.of("sensor-1"), " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SensorDefinition(null, "settings-group-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Shift shift(List<Integer> plan, List<Integer> actual) {
        return new Shift(LocalDate.of(2026, 8, 10), "sensor-1", plan, 0, actual, List.of("07:00"));
    }

    private SettingsGroup group(IntervalSetting... intervals) {
        return new SettingsGroup("settings-group-1", "Sensors 1-4", true, List.of(intervals));
    }
}
