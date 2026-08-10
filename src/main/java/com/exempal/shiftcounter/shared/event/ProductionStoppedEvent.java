package com.exempal.shiftcounter.shared.event;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;

import java.time.LocalDateTime;

public record ProductionStoppedEvent(String sensorId, LocalDateTime time,
                                     double minutes) implements DomainEvent {
    public ProductionStoppedEvent {
        SensorCatalog.require(sensorId);
        if (time == null) throw new IllegalArgumentException("time is required");
    }

    public ProductionStoppedEvent(LocalDateTime time, double minutes) {
        this(SensorCatalog.SENSOR_1, time, minutes);
    }

    public LocalDateTime getTime() { return time; }
    public double getMinutes() { return minutes; }
}
