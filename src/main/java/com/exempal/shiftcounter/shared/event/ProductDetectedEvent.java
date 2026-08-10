package com.exempal.shiftcounter.shared.event;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDetectedEvent(UUID signalId, SensorId sensorId,
                                   LocalDateTime occurredAt) implements DomainEvent {
}
