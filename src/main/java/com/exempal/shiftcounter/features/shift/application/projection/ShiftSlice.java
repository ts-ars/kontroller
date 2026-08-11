package com.exempal.shiftcounter.features.shift.application.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum ShiftSlice {
    DAY("day", "07:00–15:00", LocalTime.of(7, 0), LocalTime.of(15, 0)),
    EVENING("evening", "15:00–23:00", LocalTime.of(15, 0), LocalTime.of(7, 0));

    private final String id;
    private final String label;
    private final LocalTime start;
    private final LocalTime end;

    ShiftSlice(String id, String label, LocalTime start, LocalTime end) {
        this.id = id;
        this.label = label;
        this.start = start;
        this.end = end;
    }

    public String id() { return id; }
    public String label() { return label; }

    public boolean contains(LocalDate productionDate, LocalDateTime timestamp) {
        LocalDateTime from = productionDate.atTime(start);
        LocalDateTime to = this == DAY
                ? productionDate.atTime(end)
                : productionDate.plusDays(1).atTime(end);
        return !timestamp.isBefore(from) && timestamp.isBefore(to);
    }

    public static ShiftSlice from(String value) {
        if (value == null || value.isBlank()) return DAY;
        for (ShiftSlice slice : values()) {
            if (slice.id.equalsIgnoreCase(value)) return slice;
        }
        throw new IllegalArgumentException("Unknown shift slice: " + value);
    }
}
