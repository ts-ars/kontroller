package com.exempal.shiftcounter.features.settings.adapter.jpa;

import java.io.Serializable;
import java.util.Objects;

public final class IntervalSettingId implements Serializable {
    private String settingsGroupId;
    private int orderIndex;

    public IntervalSettingId() {}
    public IntervalSettingId(String settingsGroupId, int orderIndex) {
        this.settingsGroupId = settingsGroupId; this.orderIndex = orderIndex;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof IntervalSettingId that)) return false;
        return orderIndex == that.orderIndex && Objects.equals(settingsGroupId, that.settingsGroupId);
    }
    @Override public int hashCode() { return Objects.hash(settingsGroupId, orderIndex); }
}
