package com.exempal.shiftcounter.features.settings.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IntervalSettingJpaRepository extends JpaRepository<IntervalSettingEntity, IntervalSettingId> {
    List<IntervalSettingEntity> findBySettingsGroupIdOrderByOrderIndex(String groupId);
    void deleteBySettingsGroupId(String groupId);
}
