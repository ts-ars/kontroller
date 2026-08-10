package com.exempal.shiftcounter.features.settings.adapter.jpa;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "interval_settings")
@IdClass(IntervalSettingId.class)
public class IntervalSettingEntity {
    @Id @Column(name = "settings_group_id") private String settingsGroupId;
    @Id @Column(name = "order_index") private int orderIndex;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(nullable = false) private int plan;

    protected IntervalSettingEntity() {}
    public IntervalSettingEntity(String groupId, int order, LocalTime startTime, int plan) {
        this.settingsGroupId = groupId; this.orderIndex = order; this.startTime = startTime; this.plan = plan;
    }
    public String getSettingsGroupId() { return settingsGroupId; }
    public int getOrderIndex() { return orderIndex; }
    public LocalTime getStartTime() { return startTime; }
    public int getPlan() { return plan; }
}
