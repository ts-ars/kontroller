package com.exempal.shiftcounter.features.comment.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReconcileStoppagesCommand(LocalDate shiftDate, String sensorKey, Integer intervalIndex,
                                        LocalDateTime calculationTime, boolean resolveOnly) {
    public ReconcileStoppagesCommand(LocalDate shiftDate, String sensorKey, Integer intervalIndex,
                                     LocalDateTime calculationTime) {
        this(shiftDate, sensorKey, intervalIndex, calculationTime, false);
    }

    public static ReconcileStoppagesCommand resolveRemovedInterval(LocalDate shiftDate, String sensorKey,
                                                                    int intervalIndex,
                                                                    LocalDateTime calculationTime) {
        return new ReconcileStoppagesCommand(shiftDate, sensorKey, intervalIndex, calculationTime, true);
    }

    public ReconcileStoppagesCommand {
        if (shiftDate == null) throw new IllegalArgumentException("shiftDate is required");
        com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.require(sensorKey);
        if (calculationTime == null) throw new IllegalArgumentException("calculationTime is required");
        if (resolveOnly && intervalIndex == null) {
            throw new IllegalArgumentException("resolve-only command requires intervalIndex");
        }
    }
}
