package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import com.exempal.shiftcounter.features.settings.domain.SettingsRepository;
import com.exempal.shiftcounter.features.settings.domain.ShiftHour;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ShiftSettingsProvider {

    private final SettingsRepository settings;

    public ShiftSettingsProvider(SettingsRepository settings) {
        this.settings = settings;
    }

    public Settings get() {
        return getForSensor(SensorCatalog.SENSOR_1);
    }

    public void reload() {
        // Stage 8 deliberately has no process-local settings cache.
    }

    public Settings getForSensor(String sensorId) {
        return toSettings(settings.findById(SensorCatalog.require(sensorId).settingsGroupId()));
    }

    public SettingsGroup getGroup(String groupId) {
        return settings.findById(groupId);
    }

    private Settings toSettings(SettingsGroup group) {
        var values = group.intervals();
        var hours = new ArrayList<ShiftHour>();
        for (int index = 0; index < values.size(); index++) {
            var start = values.get(index).startTime();
            var end = index + 1 < values.size() ? values.get(index + 1).startTime()
                    : (start.getMinute() == 30 ? start.plusMinutes(30) : start.plusHours(1));
            hours.add(new ShiftHour(start, end));
        }
        return new Settings(hours, values.stream().map(value -> value.plan()).toList());
    }
}
