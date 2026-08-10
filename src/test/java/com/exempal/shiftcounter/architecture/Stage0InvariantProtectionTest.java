package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;
import com.exempal.shiftcounter.features.comment.application.StoppageMatcher;
import com.exempal.shiftcounter.features.comment.calculator.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Stage0InvariantProtectionTest {
    @Test
    void totalActualEqualsSumOfHourlyActual() {
        Shift shift = new Shift(LocalDate.of(2026, 8, 7), List.of(10, 10), 999,
                List.of(3, 4), List.of("08:00", "09:00"));
        assertEquals(7, shift.getActual());
    }

    @Test
    void intervalLossIsNeverNegative() {
        var calculation = calculator().calculate(context(100, 120));
        assertEquals(0, calculation.candidates().stream().mapToInt(StoppageCandidate::lostCans).sum());
    }

    @Test
    void fixedPlusTempoEqualsIntervalLoss() {
        var calculation = calculator().calculate(context(100, 20));
        assertEquals(80, calculation.candidates().stream().mapToInt(StoppageCandidate::lostCans).sum());
    }

    @Test
    void reconcilePreservesOperatorExplanation() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 7, 8, 0);
        Stoppage existing = new Stoppage(10L, UUID.randomUUID(), 1L, Stoppage.PRIMARY_SENSOR, 0,
                start, Duration.ofMinutes(10), 10, 10, DetectionType.FIXED, StoppageState.ACTIVE,
                List.of(new LossExplanation(20L, 10L, LossCategory.BREAKDOWN, "belt", 5, 5)), 0L);
        StoppageCalculationContext context = new StoppageCalculationContext(1L, Stoppage.PRIMARY_SENSOR,
                0, start, start.plusHours(1), 100, 20, 1.0, List.of(), start.plusHours(1));
        var plan = new StoppageMatcher().match(context, List.of(existing), List.of(
                new StoppageCandidate(DetectionType.FIXED, start.plusMinutes(1),
                        Duration.ofMinutes(10), 8)));
        assertTrue(plan.valid());
        assertEquals(existing.detectionKey(), plan.active().getFirst().detectionKey());
        assertEquals("belt", plan.active().getFirst().explanations().getFirst().comment());
        assertEquals(5, plan.active().getFirst().explanations().getFirst().allocatedMinutes());
    }

    @Test @Disabled("Known defect assigned to Stages 6–7: physical signal identity is not implemented")
    void duplicatePhysicalSignalIncrementsActualOnce() { fail("Activate after signal identity and transactions"); }

    private StoppageCalculator calculator() {
        return new StoppageCalculatorImpl(
                new StoppageFixedLossCalculator(new StoppageDetector(), Duration.ofMinutes(1)),
                new StoppageTempoLossCalculator());
    }

    private StoppageCalculationContext context(int plan, int actual) {
        LocalDateTime start = LocalDateTime.of(2026, 8, 7, 8, 0);
        return new StoppageCalculationContext(1L, Stoppage.PRIMARY_SENSOR, 0, start,
                start.plusHours(1), plan, actual, 1.0, List.of(), start.plusHours(1));
    }
}
