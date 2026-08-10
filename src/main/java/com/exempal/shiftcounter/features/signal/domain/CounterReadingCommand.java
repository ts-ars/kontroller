package com.exempal.shiftcounter.features.signal.domain;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDateTime;
import java.util.Objects;

public record CounterReadingCommand(SensorId sensorId, long currentCounter, LocalDateTime readAt) {
    public CounterReadingCommand {
        Objects.requireNonNull(sensorId, "sensorId");
        Objects.requireNonNull(readAt, "readAt");
        if (currentCounter < 0) throw new IllegalArgumentException("currentCounter must not be negative");
    }
}
