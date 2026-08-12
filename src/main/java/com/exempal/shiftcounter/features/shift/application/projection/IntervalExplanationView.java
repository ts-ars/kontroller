package com.exempal.shiftcounter.features.shift.application.projection;

public record IntervalExplanationView(String sourceSensorId, String comment, int minutes) {
    public IntervalExplanationView {
        if (sourceSensorId == null || sourceSensorId.isBlank()) {
            throw new IllegalArgumentException("sourceSensorId is required");
        }
        comment = comment == null ? "" : comment.trim();
        if (minutes < 0) throw new IllegalArgumentException("minutes must not be negative");
    }
}
