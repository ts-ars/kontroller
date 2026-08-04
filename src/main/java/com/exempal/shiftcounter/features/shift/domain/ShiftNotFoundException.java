package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;

public class ShiftNotFoundException extends RuntimeException {

    public ShiftNotFoundException(LocalDate date) {
        super("No shift found for date: " + date);
    }
}
