package com.exempal.shiftcounter.features.settings.domain;

import java.util.Comparator;
import java.util.List;
import java.time.LocalTime;
import java.util.HashSet;

public record SettingsGroup(String id, String name, boolean enabled, List<IntervalSetting> intervals) {
    public SettingsGroup {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Settings group id is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Settings group name is required");
        if (intervals == null || intervals.isEmpty()) throw new IllegalArgumentException("Intervals are required");
        intervals = intervals.stream().sorted(Comparator.comparingInt(IntervalSetting::order)).toList();
        var starts = new HashSet<LocalTime>();
        int previousOffset = -1;
        for (int index = 0; index < intervals.size(); index++) {
            IntervalSetting interval = intervals.get(index);
            if (interval.order() != index) {
                throw new IllegalArgumentException("Interval order must be contiguous from zero");
            }
            if (!starts.add(interval.startTime())) {
                throw new IllegalArgumentException("Interval start times must be unique");
            }
            int minute = interval.startTime().getHour() * 60 + interval.startTime().getMinute();
            int offset = Math.floorMod(minute - 7 * 60, 24 * 60);
            if (offset <= previousOffset) {
                throw new IllegalArgumentException("Intervals must follow production-day order");
            }
            previousOffset = offset;
        }
    }
}
