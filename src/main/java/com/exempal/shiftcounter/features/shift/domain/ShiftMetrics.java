package com.exempal.shiftcounter.features.shift.domain;

import java.time.Duration;
import java.util.List;

public record ShiftMetrics(
        List<String> labels,
        List<Integer> plans,
        List<Integer> durations,
        List<Double> canPerMinute
) {
    public Duration duration(int index) {
        return Duration.ofMinutes(durations.get(index));
    }
    /**
     * UI-only: округлённый CPM для отображения (таблицы/отчёты).
     * Не использовать в расчётах и агрегациях бизнес-логики.
     */
    @SuppressWarnings("unused")
    public int cansPerMinute(int index) {
        return (int) Math.round(canPerMinute.get(index));
    }
}