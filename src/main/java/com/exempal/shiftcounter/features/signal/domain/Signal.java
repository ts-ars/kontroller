package com.exempal.shiftcounter.features.signal.domain;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Signal(UUID id, SensorId sensorId, LocalDateTime occurredAt, LocalDate productionDate,
                     SignalSource source, String sourceIdentity) {
    public Signal(LocalDateTime occurredAt) {
        this(UUID.randomUUID(), SensorId.of("sensor-1"), occurredAt,
                occurredAt.toLocalTime().isBefore(java.time.LocalTime.of(7, 0))
                        ? occurredAt.toLocalDate().minusDays(1) : occurredAt.toLocalDate(),
                SignalSource.LEGACY, UUID.randomUUID().toString());
    }

    public LocalDateTime timestamp() {
        return occurredAt;
    }
}
