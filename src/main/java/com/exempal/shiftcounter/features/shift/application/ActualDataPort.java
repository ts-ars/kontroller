package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;
import java.util.Optional;

import static com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1;

public interface ActualDataPort {
    Shift save(Shift shift);

    Optional<Shift> findByDateAndSensorId(LocalDate date, String sensorId);

    Optional<Shift> findForUpdateByDateAndSensorId(LocalDate date, String sensorId);

    default Optional<Shift> findByDate(LocalDate date) {
        return findByDateAndSensorId(date, SENSOR_1);
    }

    void deleteByDateAndSensorId(LocalDate date, String sensorId);

    default void deleteByDate(LocalDate date) {
        deleteByDateAndSensorId(date, SENSOR_1);
    }

    Optional<Shift> findById(long shiftId);
}
