package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsAdapter implements SettingsPort {

    private final SettingsStorage storage;

    public SettingsAdapter(SettingsStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getPpm() {
        return storage.getPpm();
    }

    @Override
    public List<Integer> getHourlyPlans() {
        return storage.getHourlyPlans();
    }

    @Override
    public List<String> getHours() {
        return storage.getHours();
    }
}
