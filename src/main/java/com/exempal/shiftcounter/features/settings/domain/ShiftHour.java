package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;

public class ShiftHour {
    private final LocalTime start;
    private final LocalTime end;

    public ShiftHour(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return start + " – " + end;
    }
}
