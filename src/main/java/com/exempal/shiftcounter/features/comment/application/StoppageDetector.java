package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.application.calculator.StoppageCalculationContext;
import com.exempal.shiftcounter.features.comment.application.calculator.StoppageCandidate;
import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StoppageDetector {
    public List<StoppageCandidate> detectFixedLosses(StoppageCalculationContext context,
                                                      Duration threshold) {
        LocalDateTime detectionEnd = context.calculationTime().isBefore(context.intervalEnd())
                ? context.calculationTime() : context.intervalEnd();
        if (!detectionEnd.isAfter(context.intervalStart())) return List.of();
        List<LocalDateTime> signals = context.signalTimes().stream()
                .filter(value -> !value.isBefore(context.intervalStart()) && value.isBefore(detectionEnd))
                .sorted(Comparator.naturalOrder()).toList();
        List<StoppageCandidate> result = new ArrayList<>();
        LocalDateTime cursor = context.intervalStart();
        for (LocalDateTime signal : signals) {
            addIfLongEnough(result, cursor, Duration.between(cursor, signal), threshold);
            cursor = signal;
        }
        addIfLongEnough(result, cursor, Duration.between(cursor, detectionEnd), threshold);
        return List.copyOf(result);
    }

    private void addIfLongEnough(List<StoppageCandidate> target, LocalDateTime start,
                                 Duration duration, Duration threshold) {
        if (duration.compareTo(threshold) >= 0) {
            target.add(new StoppageCandidate(DetectionType.FIXED, start, duration, 0));
        }
    }
}
