package com.exempal.shiftcounter.features.shift.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record ShiftInterval(int index, LocalDateTime start, LocalDateTime end, boolean planSupplied) {
    public ShiftInterval {
        if (index < 0) throw new IllegalArgumentException("interval index must not be negative");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) throw new IllegalArgumentException("interval end must be after start");
    }

    public boolean contains(LocalDateTime timestamp) {
        return !timestamp.isBefore(start) && timestamp.isBefore(end);
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}
