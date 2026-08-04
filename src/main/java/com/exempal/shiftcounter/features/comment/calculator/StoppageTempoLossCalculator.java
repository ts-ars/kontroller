package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;



/**
 * Рассчитывает TEMPO-потерю на интервал: ожидание (elapsed * cpm) минус actual и уже учтённые fixedCans.
 * Основан на ShiftTimeHelper.resolveStartTime/resolveEndTime.
 */
public class StoppageTempoLossCalculator {

    private final ShiftTimeHelper timeHelper;

    public StoppageTempoLossCalculator(ShiftTimeHelper timeHelper) {
        this.timeHelper = timeHelper;
    }

    public Optional<StoppageEntry> calculateTempo(
            Shift shift,
            int hourIndex,
            int actual,
            int fixedCans,
            double cansPerMinute,
            LocalDateTime now
    ) {
        final List<String> labels = shift.getHourlyLabels();
        final LocalDate date = shift.getDate();

        if (hourIndex < 0 || hourIndex >= labels.size()) {
            return Optional.empty();
        }

        final String label = labels.get(hourIndex);
        final LocalDateTime start = timeHelper.resolveStartTime(label, date);
        final LocalDateTime end = timeHelper.resolveEndTime(labels, hourIndex, date);

        if (now.isBefore(start)) {
            return Optional.empty();
        }

        final LocalDateTime cutoff = now.isAfter(end) ? end : now;

        final long minutesElapsed = Duration.between(start, cutoff).toMinutes();
        final int expectedPlan = ( !cutoff.isBefore(end) )
                ? shift.getHourlyPlanValues().get(hourIndex)
                : (int) Math.round(minutesElapsed * cansPerMinute);

        final int residual = expectedPlan - actual - fixedCans;
        final int tempoCans = Math.max(0, residual);
        if (tempoCans == 0) return Optional.empty();

        final long tempoMinutes = Math.round(tempoCans / cansPerMinute);

        StoppageEntry tempo = StoppageEntry.tempo(
                hourIndex,
                Duration.ofMinutes(tempoMinutes),
                shift.getEntity()
        );
        tempo.setCans(tempoCans);

        return Optional.of(tempo);
    }
}