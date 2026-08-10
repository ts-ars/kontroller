package com.exempal.shiftcounter.features.shift.projection;

import java.time.LocalDate;
import java.util.List;

public record ShiftView(
        LocalDate date,
        List<Integer> actual,
        List<Integer> plan,
        List<String> hours,
        List<Boolean> planSupplied
) {
    public ShiftView(LocalDate date, List<Integer> actual, List<Integer> plan, List<String> hours) {
        this(date, actual, plan, hours, java.util.Collections.nCopies(hours.size(), true));
    }
}
