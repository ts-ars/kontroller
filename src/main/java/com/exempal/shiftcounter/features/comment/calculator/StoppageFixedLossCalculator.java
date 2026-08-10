package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.domain.Signal;

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

    public List<Stoppage> calculateFixed(Shift shift, int intervalIndex, List<Signal> signals,
                                         double cansPerMinute) {
        return detector.detectFixedLosses(shift, intervalIndex, signals, minGap).stream()
                .map(stoppage -> stoppage.withLostCans(
                        (int) Math.round(stoppage.roundedMinutes() * cansPerMinute)))
                .toList();
    }
}
