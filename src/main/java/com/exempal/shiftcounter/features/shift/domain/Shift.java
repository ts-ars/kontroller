package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;

public class Shift {

    private final Long id;
    private final LocalDate date;
    private final int actual;

    public Shift(Long id, LocalDate date, int actual) {
        this.id = id;
        this.date = date;
        this.actual = actual;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getActual() {
        return actual;
    }

    public Shift withActual(int newActual) {
        return new Shift(this.id, this.date, newActual);
    }
}
