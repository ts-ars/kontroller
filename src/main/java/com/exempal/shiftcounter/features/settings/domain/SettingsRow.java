package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;

public record SettingsRow(LocalTime hour, int sharedPlan, int sensor6Plan) {
    public SettingsRow {
        if (hour == null) throw new IllegalArgumentException("Hour is required");
        if (sharedPlan < 0 || sensor6Plan < 0) throw new IllegalArgumentException("Plan must not be negative");
    }

    public int sensor5Plan() {
        return Math.multiplyExact(sharedPlan, 4);
    }
}
