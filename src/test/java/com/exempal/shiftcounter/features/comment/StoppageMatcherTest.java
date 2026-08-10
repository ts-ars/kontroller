package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.calculator.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StoppageMatcherTest {
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 7, 8, 0);
    private final StoppageMatcher matcher = new StoppageMatcher();

    @Test
    void matchedFixedKeepsIdentityAndOperatorData() {
        Stoppage existing = persisted(10L, START.plusMinutes(5), 10, DetectionType.FIXED)
                .addExplanation(LossCategory.BREAKDOWN, "belt", 4);
        var result = matcher.match(context(), List.of(existing), List.of(
                candidate(START.plusMinutes(6), 12, 8, DetectionType.FIXED)));

        assertThat(result.valid()).isTrue();
        assertThat(result.active()).singleElement().satisfies(value -> {
            assertThat(value.detectionKey()).isEqualTo(existing.detectionKey());
            assertThat(value.explanations()).extracting(LossExplanation::comment).containsExactly("belt");
            assertThat(value.explanations()).extracting(LossExplanation::allocatedMinutes).containsExactly(4);
        });
    }

    @Test
    void identicalInputIsPersistenceIdempotent() {
        Stoppage existing = persisted(10L, START.plusMinutes(5), 10, DetectionType.FIXED);
        var result = matcher.match(context(), List.of(existing), List.of(
                candidate(START.plusMinutes(5), 10, 10, DetectionType.FIXED)));
        assertThat(result.toSave()).isEmpty();
        assertThat(result.active()).singleElement().satisfies(value -> {
            assertThat(value.id()).isEqualTo(existing.id());
            assertThat(value.detectionKey()).isEqualTo(existing.detectionKey());
            assertThat(value.version()).isEqualTo(existing.version());
        });
    }

    @Test
    void missingRangeIsResolvedInsteadOfDeleted() {
        Stoppage existing = persisted(10L, START.plusMinutes(5), 10, DetectionType.FIXED);
        var result = matcher.match(context(), List.of(existing), List.of());
        assertThat(result.toSave()).singleElement()
                .extracting(Stoppage::state).isEqualTo(StoppageState.RESOLVED);
    }

    @Test
    void equalBestOverlapIsReportedAndNothingIsWritten() {
        Stoppage left = persisted(10L, START.plusMinutes(5), 10, DetectionType.FIXED);
        Stoppage right = persisted(11L, START.plusMinutes(15), 10, DetectionType.FIXED);
        var result = matcher.match(context(), List.of(left, right), List.of(
                candidate(START.plusMinutes(10), 10, 10, DetectionType.FIXED)));
        assertThat(result.valid()).isFalse();
        assertThat(result.toSave()).isEmpty();
        assertThat(result.diagnostics()).extracting(ReconcileDiagnostic::code)
                .contains(ReconcileDiagnosticCode.AMBIGUOUS_FIXED_MATCH);
    }

    @Test
    void adjacentBoundaryPartReusesIncidentIdentity() {
        UUID incident = UUID.randomUUID();
        Stoppage previous = new Stoppage(10L, UUID.randomUUID(), incident, 1L, Stoppage.PRIMARY_SENSOR,
                0, START.minusMinutes(10), Duration.ofMinutes(10), 10, 10, DetectionType.FIXED,
                StoppageState.ACTIVE, List.of(), 0L);
        StoppageCalculationContext second = new StoppageCalculationContext(1L, Stoppage.PRIMARY_SENSOR,
                1, START, START.plusHours(1), 100, 0, 1.0, List.of(), START.plusHours(1));
        var result = matcher.match(second, List.of(previous), List.of(
                candidate(START, 10, 10, DetectionType.FIXED)));
        assertThat(result.active()).singleElement().extracting(Stoppage::incidentKey).isEqualTo(incident);
    }

    private Stoppage persisted(long id, LocalDateTime start, int minutes, DetectionType type) {
        UUID key = UUID.randomUUID();
        return new Stoppage(id, key, key, 1L, Stoppage.PRIMARY_SENSOR, 0, start,
                Duration.ofMinutes(minutes), minutes, 10, type, StoppageState.ACTIVE, List.of(), 0L);
    }

    private StoppageCandidate candidate(LocalDateTime start, int minutes, int cans, DetectionType type) {
        return new StoppageCandidate(type, start, Duration.ofMinutes(minutes), cans);
    }

    private StoppageCalculationContext context() {
        return new StoppageCalculationContext(1L, Stoppage.PRIMARY_SENSOR, 0, START,
                START.plusHours(1), 100, 0, 1.0, List.of(), START.plusHours(1));
    }
}
