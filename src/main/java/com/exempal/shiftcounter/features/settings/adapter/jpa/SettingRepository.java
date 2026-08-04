package com.exempal.shiftcounter.features.settings.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<SettingEntity, String> {
}
