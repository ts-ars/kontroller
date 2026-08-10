package com.exempal.shiftcounter.features.settings.adapter.jpa;

import com.exempal.shiftcounter.features.settings.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaSettingsGroupAdapter implements SettingsRepository {
    private final SettingsGroupJpaRepository groups;
    private final IntervalSettingJpaRepository intervals;

    public JpaSettingsGroupAdapter(SettingsGroupJpaRepository groups, IntervalSettingJpaRepository intervals) {
        this.groups = groups; this.intervals = intervals;
    }

    @Override @Transactional(readOnly = true)
    public SettingsGroup findById(String groupId) {
        return map(groups.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Unknown settings group: " + groupId)));
    }

    @Override @Transactional
    public SettingsGroup findByIdForUpdate(String groupId) {
        return map(groups.findByIdForUpdate(groupId).orElseThrow(() -> new IllegalArgumentException("Unknown settings group: " + groupId)));
    }

    @Override @Transactional
    public void save(SettingsGroup group) {
        SettingsGroupEntity entity = groups.findById(group.id())
                .orElseGet(() -> new SettingsGroupEntity(group.id(), group.name(), group.enabled()));
        entity.update(group.name(), group.enabled());
        groups.save(entity);
        intervals.deleteBySettingsGroupId(group.id());
        intervals.flush();
        intervals.saveAll(group.intervals().stream()
                .map(value -> new IntervalSettingEntity(group.id(), value.order(), value.startTime(), value.plan()))
                .toList());
    }

    private SettingsGroup map(SettingsGroupEntity entity) {
        var values = intervals.findBySettingsGroupIdOrderByOrderIndex(entity.getId()).stream()
                .map(value -> new IntervalSetting(value.getStartTime(), value.getPlan(), value.getOrderIndex()))
                .toList();
        return new SettingsGroup(entity.getId(), entity.getName(), entity.isEnabled(), values);
    }
}
