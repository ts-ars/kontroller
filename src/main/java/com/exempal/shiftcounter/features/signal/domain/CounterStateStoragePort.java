package com.exempal.shiftcounter.features.signal.domain;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CounterStateStoragePort {
    CounterStateLoad getOrInitializeForUpdate(SensorId sensorId, long currentCounter,
                                              LocalDateTime readAt, LocalDate productionDate);

    void save(CounterState state);
}
