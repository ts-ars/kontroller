package com.exempal.shiftcounter.features.shift.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ShiftReconcilePort {
    void reconcile(LocalDate shiftDate, String sensorId, int intervalIndex, LocalDateTime calculationTime);

    void resolveRemovedInterval(LocalDate shiftDate, String sensorId, int intervalIndex,
                                LocalDateTime calculationTime);
}
