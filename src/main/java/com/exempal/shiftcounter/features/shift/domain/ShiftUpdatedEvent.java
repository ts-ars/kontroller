package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.util.List;

public record ShiftUpdatedEvent(
        LocalDate date,
        String sensorId,
        List<Integer> actual,
        List<Integer> plan,
        List<String> hours
) {
    public ShiftUpdatedEvent(LocalDate date, List<Integer> actual, List<Integer> plan, List<String> hours) {
        this(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1,
                actual, plan, hours);
    }
}
