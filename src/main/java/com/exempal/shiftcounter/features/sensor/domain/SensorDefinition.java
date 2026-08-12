package com.exempal.shiftcounter.features.sensor.domain;

public record SensorDefinition(SensorId id, String settingsGroupId, PlanRole planRole, int planMultiplier) {
    public SensorDefinition(SensorId id, String settingsGroupId) {
        this(id, settingsGroupId, PlanRole.SHARED, 1);
    }

    public SensorDefinition {
        if (id == null) throw new IllegalArgumentException("Sensor id is required");
        if (settingsGroupId == null || settingsGroupId.isBlank()) {
            throw new IllegalArgumentException("A sensor must belong to exactly one settings group");
        }
        if (planRole == null) throw new IllegalArgumentException("Plan role is required");
        if (planMultiplier < 1) throw new IllegalArgumentException("Plan multiplier must be positive");
        if (planRole != PlanRole.DERIVED && planMultiplier != 1) {
            throw new IllegalArgumentException("Only a derived plan may have a multiplier");
        }
    }
}
