package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ActualDataPort {
    void save(Shift shift);

    Optional<Shift> findByDate(LocalDate date);

    List<Integer> getHourlyActuals(LocalDate date);

    void incrementHourlyActual(LocalDate date, LocalTime time);
}

