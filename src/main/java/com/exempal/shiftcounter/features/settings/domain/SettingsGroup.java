package com.exempal.shiftcounter.features.settings.domain;

import java.util.Comparator;
import java.util.List;

public record SettingsGroup(String id, String name, boolean enabled, List<IntervalSetting> intervals) {
    public SettingsGroup {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Settings group id is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Settings group name is required");
        if (intervals == null || intervals.isEmpty()) throw new IllegalArgumentException("Intervals are required");
        intervals = intervals.stream().sorted(Comparator.comparingInt(IntervalSetting::order)).toList();
        for (int index = 0; index < intervals.size(); index++) {
            if (intervals.get(index).order() != index) {
                throw new IllegalArgumentException("Interval order must be contiguous from zero");
            }
        }
    }
}
