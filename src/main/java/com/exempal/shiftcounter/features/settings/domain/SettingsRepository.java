package com.exempal.shiftcounter.features.settings.domain;

public interface SettingsRepository {
    SettingsGroup findById(String groupId);
    SettingsGroup findByIdForUpdate(String groupId);
    void save(SettingsGroup group);
}
