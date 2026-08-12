package com.exempal.shiftcounter.features.sensor.domain;

import java.util.List;

public final class SensorCatalog {
    public static final String SENSOR_1 = "sensor-1";
    public static final String SENSOR_5 = "sensor-5";
    public static final String SENSOR_6 = "sensor-6";
    public static final String SHARED_SETTINGS_GROUP = "settings-group-1";
    public static final String INDEPENDENT_SETTINGS_GROUP = "settings-group-2";
    public static final String GROUP_1 = SHARED_SETTINGS_GROUP;
    public static final String GROUP_2 = INDEPENDENT_SETTINGS_GROUP;

    private static final List<SensorDefinition> SENSORS = List.of(
            sensor(1, SHARED_SETTINGS_GROUP, PlanRole.SHARED, 1),
            sensor(2, SHARED_SETTINGS_GROUP, PlanRole.SHARED, 1),
            sensor(3, SHARED_SETTINGS_GROUP, PlanRole.SHARED, 1),
            sensor(4, SHARED_SETTINGS_GROUP, PlanRole.SHARED, 1),
            sensor(5, SHARED_SETTINGS_GROUP, PlanRole.DERIVED, 4),
            sensor(6, INDEPENDENT_SETTINGS_GROUP, PlanRole.INDEPENDENT, 1));

    private SensorCatalog() {
    }

    public static List<SensorDefinition> all() {
        return SENSORS;
    }

    public static SensorDefinition require(String sensorId) {
        return SENSORS.stream().filter(sensor -> sensor.id().value().equals(sensorId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown sensor id: " + sensorId));
    }

    private static SensorDefinition sensor(int number, String group, PlanRole role, int multiplier) {
        return new SensorDefinition(new SensorId("sensor-" + number), group, role, multiplier);
    }
}
