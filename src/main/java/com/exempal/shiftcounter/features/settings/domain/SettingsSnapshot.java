package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record SettingsSnapshot(List<SettingsRow> rows) {
    public static final int STANDARD_ROW_COUNT = 16;
    private static final int PRODUCTION_BOUNDARY_MINUTES = 7 * 60;

    public SettingsSnapshot {
        if (rows == null || rows.isEmpty()) throw new IllegalArgumentException("Settings rows are required");
        rows = List.copyOf(rows);
        int previousOffset = -1;
        for (SettingsRow row : rows) {
            int offset = productionOffset(row.hour());
            if (offset <= previousOffset) {
                throw new IllegalArgumentException("Hours must be unique and follow production-day order");
            }
            previousOffset = offset;
        }
    }

    public int sharedTotal() {
        return rows.stream().mapToInt(SettingsRow::sharedPlan).sum();
    }

    public int sensor5Total() {
        return Math.multiplyExact(sharedTotal(), 4);
    }

    public int sensor6Total() {
        return rows.stream().mapToInt(SettingsRow::sensor6Plan).sum();
    }

    public SettingsSnapshot addHour() {
        SettingsRow tail = rows.getLast();
        LocalTime nextHour = tail.hour().plusHours(1);
        if (productionOffset(nextHour) <= productionOffset(tail.hour())) {
            throw new IllegalStateException("Settings cannot extend across the 07:00 production-day boundary");
        }
        SettingsRow full = nearestPreviousFullInterval();
        List<SettingsRow> updated = new ArrayList<>(rows);
        updated.set(updated.size() - 1, new SettingsRow(tail.hour(), full.sharedPlan(), full.sensor6Plan()));
        updated.add(new SettingsRow(nextHour, tail.sharedPlan(), tail.sensor6Plan()));
        return new SettingsSnapshot(updated);
    }

    public SettingsSnapshot deleteLastExtension() {
        if (rows.size() <= STANDARD_ROW_COUNT) {
            throw new IllegalStateException("Only the last extension row can be deleted");
        }
        List<SettingsRow> updated = new ArrayList<>(rows);
        SettingsRow tail = updated.removeLast();
        SettingsRow previous = updated.getLast();
        updated.set(updated.size() - 1,
                new SettingsRow(previous.hour(), tail.sharedPlan(), tail.sensor6Plan()));
        return new SettingsSnapshot(updated);
    }

    private SettingsRow nearestPreviousFullInterval() {
        int maxShared = rows.stream().mapToInt(SettingsRow::sharedPlan).max().orElseThrow();
        int maxSensor6 = rows.stream().mapToInt(SettingsRow::sensor6Plan).max().orElseThrow();
        for (int index = rows.size() - 2; index >= 0; index--) {
            SettingsRow candidate = rows.get(index);
            if (candidate.sharedPlan() == maxShared && candidate.sensor6Plan() == maxSensor6) {
                return candidate;
            }
        }
        throw new IllegalStateException("A previous full interval is required before the tail");
    }

    private static int productionOffset(LocalTime value) {
        return Math.floorMod(value.getHour() * 60 + value.getMinute() - PRODUCTION_BOUNDARY_MINUTES,
                24 * 60);
    }
}
