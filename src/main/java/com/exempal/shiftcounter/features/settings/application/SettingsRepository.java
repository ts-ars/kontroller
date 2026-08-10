package com.exempal.shiftcounter.features.settings.application;

import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;

public interface SettingsRepository {
    SettingsGroup findById(String groupId);
    SettingsGroup findByIdForUpdate(String groupId);
    void save(SettingsGroup group);
}
