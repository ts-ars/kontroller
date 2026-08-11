package com.exempal.shiftcounter.features.sensor;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.sensor.domain.PlanRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensorCatalogTest {
    @Test
    void exposesSixStableSensorsWithExplicitPlanOwnership() {
        assertThat(SensorCatalog.all()).extracting(sensor -> sensor.id().value())
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-5", "sensor-6");
        assertThat(SensorCatalog.all().subList(0, 4)).allMatch(sensor ->
                sensor.settingsGroupId().equals(SensorCatalog.SHARED_SETTINGS_GROUP)
                        && sensor.planRole() == PlanRole.SHARED);
        assertThat(SensorCatalog.require("sensor-5").settingsGroupId())
                .isEqualTo(SensorCatalog.SHARED_SETTINGS_GROUP);
        assertThat(SensorCatalog.require("sensor-5").planRole()).isEqualTo(PlanRole.DERIVED);
        assertThat(SensorCatalog.require("sensor-5").planMultiplier()).isEqualTo(4);
        assertThat(SensorCatalog.require("sensor-6").settingsGroupId())
                .isEqualTo(SensorCatalog.INDEPENDENT_SETTINGS_GROUP);
        assertThat(SensorCatalog.require("sensor-6").planRole()).isEqualTo(PlanRole.INDEPENDENT);
    }
}
