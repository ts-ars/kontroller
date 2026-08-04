package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface ActualDataPort {
    Shift save(Shift shift);

    Optional<Shift> findByDate(LocalDate date);

    void saveOrReplace(Shift shift);

    void deleteByDate(LocalDate date);

    Optional<Shift> findById(long shiftId);
}
