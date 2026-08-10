package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;

public interface ShiftInitializer {
    Shift createNewShift(LocalDate date, String sensorId);

    default Shift createNewShift(LocalDate date) {
        return createNewShift(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

}
