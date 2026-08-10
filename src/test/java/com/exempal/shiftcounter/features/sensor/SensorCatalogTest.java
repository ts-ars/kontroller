package com.exempal.shiftcounter.features.sensor;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensorCatalogTest {
    @Test
    void exposesSixStableSensorsInApprovedGroups() {
        assertThat(SensorCatalog.all()).extracting(sensor -> sensor.id().value())
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-5", "sensor-6");
        assertThat(SensorCatalog.all().subList(0, 4)).allMatch(sensor ->
                sensor.settingsGroupId().equals("settings-group-1"));
        assertThat(SensorCatalog.all().subList(4, 6)).allMatch(sensor ->
                sensor.settingsGroupId().equals("settings-group-2"));
    }
}
