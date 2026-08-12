package com.exempal.shiftcounter.features.shift.application.projection;

import java.time.LocalDate;
import java.util.List;

public record ShiftView(
        LocalDate date,
        String sensorId,
        List<Integer> actual,
        List<Integer> plan,
        List<String> hours,
        List<Boolean> planSupplied,
        List<List<IntervalExplanationView>> explanations
) {
    public ShiftView(LocalDate date, List<Integer> actual, List<Integer> plan, List<String> hours) {
        this(date, "sensor-1", actual, plan, hours, java.util.Collections.nCopies(hours.size(), true),
                emptyExplanations(hours.size()));
    }

    public ShiftView(LocalDate date, List<Integer> actual, List<Integer> plan, List<String> hours,
                     List<Boolean> planSupplied) {
        this(date, "sensor-1", actual, plan, hours, planSupplied, emptyExplanations(hours.size()));
    }

    public ShiftView(LocalDate date, String sensorId, List<Integer> actual, List<Integer> plan,
                     List<String> hours, List<Boolean> planSupplied) {
        this(date, sensorId, actual, plan, hours, planSupplied, emptyExplanations(hours.size()));
    }

    public ShiftView {
        actual = List.copyOf(actual);
        plan = List.copyOf(plan);
        hours = List.copyOf(hours);
        planSupplied = List.copyOf(planSupplied);
        explanations = explanations.stream().map(List::copyOf).toList();
        int size = hours.size();
        if (actual.size() != size || plan.size() != size || planSupplied.size() != size
                || explanations.size() != size) {
            throw new IllegalArgumentException("ShiftView interval columns must have equal sizes");
        }
    }

    private static List<List<IntervalExplanationView>> emptyExplanations(int size) {
        return java.util.stream.IntStream.range(0, size).mapToObj(index -> List.<IntervalExplanationView>of()).toList();
    }
}
