package com.exempal.shiftcounter.features.signal.domain;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CounterState(SensorId sensorId, long lastCounterValue, LocalDateTime lastReadAt,
                           LocalDate productionDate, CounterContinuity continuity) {
    public CounterState {
        if (lastCounterValue < 0) throw new IllegalArgumentException("counter value must not be negative");
    }
}
