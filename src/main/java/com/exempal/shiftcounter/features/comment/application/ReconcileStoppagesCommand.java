package com.exempal.shiftcounter.features.comment.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReconcileStoppagesCommand(LocalDate shiftDate, String sensorKey, Integer intervalIndex,
                                        LocalDateTime calculationTime) {
    public ReconcileStoppagesCommand {
        if (shiftDate == null) throw new IllegalArgumentException("shiftDate is required");
        if (sensorKey == null || sensorKey.isBlank()) throw new IllegalArgumentException("sensorKey is required");
        if (calculationTime == null) throw new IllegalArgumentException("calculationTime is required");
    }
}
