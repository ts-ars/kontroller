package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public record ProductionDay(LocalDate date, LocalDateTime start, LocalDateTime end) {
    public static final LocalTime BOUNDARY = LocalTime.of(7, 0);

    public ProductionDay {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.isAfter(start)) throw new IllegalArgumentException("production day end must be after start");
    }

    public static ProductionDay of(LocalDate date) {
        return new ProductionDay(date, date.atTime(BOUNDARY), date.plusDays(1).atTime(BOUNDARY));
    }

    public boolean contains(LocalDateTime timestamp) {
        return !timestamp.isBefore(start) && timestamp.isBefore(end);
    }
}
