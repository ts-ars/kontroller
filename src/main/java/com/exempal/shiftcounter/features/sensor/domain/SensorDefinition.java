package com.exempal.shiftcounter.features.sensor.domain;

public record SensorDefinition(SensorId id, String settingsGroupId) {
    public SensorDefinition {
        if (id == null) throw new IllegalArgumentException("Sensor id is required");
        if (settingsGroupId == null || settingsGroupId.isBlank()) {
            throw new IllegalArgumentException("A sensor must belong to exactly one settings group");
        }
    }
}
