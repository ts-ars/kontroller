package com.exempal.shiftcounter.features.settings.adapter.settings;

import com.exempal.shiftcounter.features.settings.application.SettingsRepository;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ShiftSettings;
import com.exempal.shiftcounter.features.shift.application.ShiftSettingsPort;
import org.springframework.stereotype.Component;

@Component
public class ShiftSettingsProvider implements ShiftSettingsPort {

    private final SettingsRepository settings;

    public ShiftSettingsProvider(SettingsRepository settings) {
        this.settings = settings;
    }

    @Override
    public ShiftSettings getForSensor(String sensorId) {
        var sensor = SensorCatalog.require(sensorId);
        return toShiftSettings(settings.findById(sensor.settingsGroupId()), sensor.planMultiplier());
    }

    private ShiftSettings toShiftSettings(SettingsGroup group, int multiplier) {
        var values = group.intervals();
        return new ShiftSettings(values.stream().map(value -> value.startTime().toString()).toList(),
                values.stream().map(value -> Math.multiplyExact(value.plan(), multiplier)).toList());
    }
}
