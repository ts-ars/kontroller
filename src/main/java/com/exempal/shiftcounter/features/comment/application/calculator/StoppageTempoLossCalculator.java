package com.exempal.shiftcounter.features.comment.application.calculator;

import com.exempal.shiftcounter.features.comment.domain.DetectionType;

import java.time.Duration;
import java.util.Optional;

public class StoppageTempoLossCalculator {
    public Optional<StoppageCandidate> calculateTempo(StoppageCalculationContext context, int tempoCans) {
        if (tempoCans <= 0 || context.cansPerMinute() <= 0) return Optional.empty();
        Duration duration = Duration.ofSeconds(Math.round(tempoCans * 60.0 / context.cansPerMinute()));
        return Optional.of(new StoppageCandidate(DetectionType.TEMPO, context.intervalStart(),
                duration, tempoCans));
    }
}
