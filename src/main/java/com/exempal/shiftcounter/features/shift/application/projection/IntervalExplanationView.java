package com.exempal.shiftcounter.features.shift.application.projection;

public record IntervalExplanationView(String sourceSensorId, String comment, int minutes, int cans, String status) {
    public IntervalExplanationView(String sourceSensorId, String comment, int minutes) {
        this(sourceSensorId, comment, minutes, 0, "EXPLAINED");
    }

    public IntervalExplanationView {
        if (sourceSensorId == null || sourceSensorId.isBlank()) {
            throw new IllegalArgumentException("sourceSensorId is required");
        }
        comment = comment == null ? "" : comment.trim();
        if (minutes < 0) throw new IllegalArgumentException("minutes must not be negative");
        if (cans < 0) throw new IllegalArgumentException("cans must not be negative");
        status = status == null || status.isBlank() ? "EXPLAINED" : status;
    }
}
