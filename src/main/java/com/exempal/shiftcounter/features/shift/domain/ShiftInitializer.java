package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;

public interface ShiftInitializer {
    Shift createNewShift(LocalDate date);
    Shift recalculateShift(LocalDate date);
}
