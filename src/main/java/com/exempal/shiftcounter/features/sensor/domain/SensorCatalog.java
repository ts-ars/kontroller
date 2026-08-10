package com.exempal.shiftcounter.features.sensor.domain;

import java.util.List;

public final class SensorCatalog {
    public static final String SENSOR_1 = "sensor-1";
    public static final String GROUP_1 = "settings-group-1";
    public static final String GROUP_2 = "settings-group-2";

    private static final List<SensorDefinition> SENSORS = List.of(
            sensor(1, GROUP_1), sensor(2, GROUP_1), sensor(3, GROUP_1), sensor(4, GROUP_1),
            sensor(5, GROUP_2), sensor(6, GROUP_2));

    private SensorCatalog() {
    }

    public static List<SensorDefinition> all() {
        return SENSORS;
    }

    public static SensorDefinition require(String sensorId) {
        return SENSORS.stream().filter(sensor -> sensor.id().value().equals(sensorId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown sensor id: " + sensorId));
    }

    private static SensorDefinition sensor(int number, String group) {
        return new SensorDefinition(new SensorId("sensor-" + number), group);
    }
}
