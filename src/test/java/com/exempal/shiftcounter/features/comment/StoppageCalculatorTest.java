package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.application.calculator.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoppageCalculatorTest {
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 7, 8, 0);

    @Test
    void currentIntervalUsesFullPlan() {
        StoppageCalculation result = calculator(Duration.ofHours(2)).calculate(
                context(60, 0, 1.0, List.of(), START.plusMinutes(30)));

        assertThat(result.candidates()).singleElement().satisfies(value -> {
            assertThat(value.detectionType()).isEqualTo(DetectionType.TEMPO);
            assertThat(value.lostCans()).isEqualTo(60);
        });
    }

    @Test
    void fixedIsBoundedByTotalLoss() {
        StoppageCalculation result = calculator(Duration.ofMinutes(1)).calculate(
                context(10, 0, 1.0, List.of(), START.plusHours(1)));

        assertThat(result.candidates()).singleElement().satisfies(value -> {
            assertThat(value.detectionType()).isEqualTo(DetectionType.FIXED);
            assertThat(value.lostCans()).isEqualTo(10);
        });
        assertThat(result.diagnostics()).extracting(ReconcileDiagnostic::code)
                .contains(ReconcileDiagnosticCode.FIXED_EXCEEDS_TOTAL_LOSS);
    }

    @Test
    void tempoIsExactResidualAfterFixed() {
        List<LocalDateTime> signals = new ArrayList<>();
        for (int second = 600; second < 3600; second += 30) signals.add(START.plusSeconds(second));
        StoppageCalculation result = calculator(Duration.ofMinutes(1)).calculate(
                context(100, 60, 100.0 / 60.0, signals, START.plusHours(1)));

        int fixed = result.candidates().stream().filter(v -> v.detectionType() == DetectionType.FIXED)
                .mapToInt(StoppageCandidate::lostCans).sum();
        int tempo = result.candidates().stream().filter(v -> v.detectionType() == DetectionType.TEMPO)
                .mapToInt(StoppageCandidate::lostCans).sum();
        assertThat(fixed).isEqualTo(17);
        assertThat(tempo).isEqualTo(23);
        assertThat(fixed + tempo).isEqualTo(40);
    }

    private StoppageCalculator calculator(Duration threshold) {
        return new StoppageCalculatorImpl(new StoppageFixedLossCalculator(new StoppageDetector(), threshold),
                new StoppageTempoLossCalculator());
    }

    private StoppageCalculationContext context(int plan, int actual, double cpm,
                                                List<LocalDateTime> signals, LocalDateTime now) {
        return new StoppageCalculationContext(1L, Stoppage.PRIMARY_SENSOR, 0, START,
                START.plusHours(1), plan, actual, cpm, signals, now);
    }
}
