package com.exempal.shiftcounter.features.sensor.domain;

public record SensorId(String value) {
    public SensorId {
        if (value == null || !value.matches("sensor-[1-6]")) {
            throw new IllegalArgumentException("Unknown sensor id: " + value);
        }
    }

    public static SensorId of(String value) {
        return new SensorId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
