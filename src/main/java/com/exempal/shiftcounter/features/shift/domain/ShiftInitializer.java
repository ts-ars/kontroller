package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;

public interface ShiftInitializer {
    Shift createNewShift(LocalDate date, String sensorId);
    Shift recalculateShift(LocalDate date, String sensorId);

    default Shift createNewShift(LocalDate date) {
        return createNewShift(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    default Shift recalculateShift(LocalDate date) {
        return recalculateShift(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }
}
