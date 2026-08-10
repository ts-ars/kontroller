package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;
import java.util.Optional;

public interface ReconcileShiftRepository {
    Optional<Shift> findForUpdateByDate(LocalDate date);
}
