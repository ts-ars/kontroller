package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import org.springframework.stereotype.Component;

@Component
public class ShiftSettingsProvider {

    private final SettingsPort settingsPort;
    private volatile Settings current;

    public ShiftSettingsProvider(SettingsPort settingsPort) {
        this.settingsPort = settingsPort;
        this.current = settingsPort.load(); // загрузка при старте приложения
    }

    public Settings get() {
        return current;
    }

    public void reload() {
        this.current = settingsPort.load();
    }
}
