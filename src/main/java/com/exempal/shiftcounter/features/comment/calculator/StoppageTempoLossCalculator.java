package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StoppageTempoLossCalculator {
    private final ShiftTimeHelper timeHelper;

    public StoppageTempoLossCalculator(ShiftTimeHelper timeHelper) {
        this.timeHelper = timeHelper;
    }

    public Optional<Stoppage> calculateTempo(Shift shift, int intervalIndex, int actual, int fixedCans,
                                              double cansPerMinute, LocalDateTime now) {
        List<String> labels = shift.getHourlyLabels();
        if (intervalIndex < 0 || intervalIndex >= labels.size() || cansPerMinute <= 0) return Optional.empty();
        LocalDateTime start = timeHelper.resolveStartTime(labels.get(intervalIndex), shift.getDate());
        LocalDateTime end = timeHelper.resolveEndTime(labels, intervalIndex, shift.getDate());
        if (now.isBefore(start)) return Optional.empty();
        LocalDateTime cutoff = now.isAfter(end) ? end : now;
        long elapsedMinutes = Duration.between(start, cutoff).toMinutes();
        int expectedPlan = !cutoff.isBefore(end) ? shift.getHourlyPlanValues().get(intervalIndex)
                : (int) Math.round(elapsedMinutes * cansPerMinute);
        int tempoCans = Math.max(0, expectedPlan - actual - fixedCans);
        if (tempoCans == 0) return Optional.empty();
        Duration duration = Duration.ofSeconds(Math.round(tempoCans * 60.0 / cansPerMinute));
        long shiftId = shift.getId() != null ? shift.getId() : shift.getEntity().getId();
        return Optional.of(Stoppage.detected(UUID.randomUUID(), shiftId, Stoppage.PRIMARY_SENSOR,
                intervalIndex, start, duration, tempoCans, DetectionType.TEMPO));
    }
}
