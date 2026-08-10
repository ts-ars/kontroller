package com.exempal.shiftcounter.features.comment.calculator;

import java.time.LocalDateTime;
import java.util.List;

public record StoppageCalculationContext(
        long shiftId,
        String sensorKey,
        int intervalIndex,
        LocalDateTime intervalStart,
        LocalDateTime intervalEnd,
        int plan,
        int actual,
        double cansPerMinute,
        List<LocalDateTime> signalTimes,
        LocalDateTime calculationTime
) {
    public StoppageCalculationContext {
        signalTimes = List.copyOf(signalTimes);
    }
}
