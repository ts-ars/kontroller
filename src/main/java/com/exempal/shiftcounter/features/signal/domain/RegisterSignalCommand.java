package com.exempal.shiftcounter.features.signal.domain;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDateTime;

public record RegisterSignalCommand(SensorId sensorId, LocalDateTime occurredAt,
                                    SignalSource source, String sourceIdentity) {
    public RegisterSignalCommand {
        if (sensorId == null) throw new IllegalArgumentException("sensorId is required");
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (source == null) throw new IllegalArgumentException("source is required");
        if (sourceIdentity == null || sourceIdentity.isBlank()) {
            throw new IllegalArgumentException("sourceIdentity is required");
        }
    }
}
