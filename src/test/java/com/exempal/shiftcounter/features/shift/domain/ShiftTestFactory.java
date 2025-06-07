package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;

/**
 * Test factory for creating Shift instances with default or custom values.
 * Intended to reduce boilerplate in unit and integration tests.
 */
public class ShiftTestFactory {

    public static Shift ok() {
        return new Shift(LocalDate.now(), 100, 100, "OK");
    }

    public static Shift underperformed() {
        return new Shift(LocalDate.now(), 200, 150, "Недовыполнение");
    }

    public static Shift with(LocalDate date, int planned, int actual, String comment) {
        return new Shift(date, planned, actual, comment);
    }

    public static Shift withPlanAndActual(int planned, int actual) {
        String comment = actual >= planned ? "OK" : "Недовыполнение";
        return new Shift(LocalDate.now(), planned, actual, comment);
    }

    public static Shift empty(LocalDate date) {
        return new Shift(date, 0, 0, "");
    }

    // future potential cases:
    public static Shift manualComment(String comment) {
        return new Shift(LocalDate.now(), 100, 90, comment);
    }
}
