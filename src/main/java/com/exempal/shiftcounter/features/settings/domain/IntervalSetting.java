package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;

public record IntervalSetting(LocalTime startTime, int plan, int order) {
    public IntervalSetting {
        if (startTime == null) throw new IllegalArgumentException("Time is required");
        if (plan < 0) throw new IllegalArgumentException("Plan must not be negative");
        if (order < 0) throw new IllegalArgumentException("Order must not be negative");
    }
}
