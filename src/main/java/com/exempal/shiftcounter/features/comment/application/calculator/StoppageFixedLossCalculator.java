package com.exempal.shiftcounter.features.comment.application.calculator;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;

import java.time.Duration;
import java.util.List;

public class StoppageFixedLossCalculator {
    private final StoppageDetector detector;
    private final Duration minGap;

    public StoppageFixedLossCalculator(StoppageDetector detector, Duration minGap) {
        if (minGap == null || minGap.isZero() || minGap.isNegative()) {
            throw new IllegalArgumentException("minGap must be positive");
        }
        this.detector = detector;
        this.minGap = minGap;
    }

    public List<StoppageCandidate> calculateFixed(StoppageCalculationContext context) {
        return detector.detectFixedLosses(context, minGap).stream()
                .map(value -> value.withLostCans((int) Math.round(
                        value.exactDuration().toNanos() / 60_000_000_000.0 * context.cansPerMinute())))
                .toList();
    }
}
