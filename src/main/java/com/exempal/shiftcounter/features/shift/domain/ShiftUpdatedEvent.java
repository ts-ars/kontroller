package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.util.List;

public record ShiftUpdatedEvent(
        LocalDate date,
        List<Integer> actual,
        List<Integer> plan,
        List<String> hours
) {}