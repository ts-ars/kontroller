package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class StoppageModelTest {
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 7, 8, 0, 30);

    @Test
    void derivesAllExplanationStatuses() {
        Stoppage unexplained = stoppage(List.of(), Duration.ofMinutes(10));
        Stoppage partial = unexplained.addExplanation(LossCategory.MATERIAL, "roll", 4);
        Stoppage full = partial.addExplanation(LossCategory.QUALITY, "quality", 6);
        Stoppage conflict = full.withSystemMeasurement(START, Duration.ofMinutes(5), 50);

        assertThat(unexplained.explanationStatus()).isEqualTo(ExplanationStatus.UNEXPLAINED);
        assertThat(partial.explanationStatus()).isEqualTo(ExplanationStatus.PARTIALLY_EXPLAINED);
        assertThat(full.explanationStatus()).isEqualTo(ExplanationStatus.FULLY_EXPLAINED);
        assertThat(conflict.explanationStatus()).isEqualTo(ExplanationStatus.ALLOCATION_CONFLICT);
        assertThat(conflict.explanations()).extracting(LossExplanation::category,
                        LossExplanation::comment, LossExplanation::allocatedMinutes)
                .containsExactly(
                        tuple(LossCategory.MATERIAL, "roll", 4),
                        tuple(LossCategory.QUALITY, "quality", 6));
    }

    @Test
    void normalEditCannotOverAllocate() {
        Stoppage value = stoppage(List.of(), Duration.ofMinutes(10))
                .addExplanation(LossCategory.MATERIAL, "roll", 7);
        assertThatThrownBy(() -> value.addExplanation(LossCategory.QUALITY, "too much", 4))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("exceed");
    }

    @Test
    void resolvedLossRetainsStableIdentityAndExplanations() {
        Stoppage active = stoppage(List.of(), Duration.ofMinutes(10))
                .addExplanation(LossCategory.BREAKDOWN, "belt", 10);
        Stoppage resolved = active.resolve();
        assertThat(resolved.state()).isEqualTo(StoppageState.RESOLVED);
        assertThat(resolved.detectionKey()).isEqualTo(active.detectionKey());
        assertThat(resolved.explanations()).isEqualTo(active.explanations());
    }

    @Test
    void ownsExactTimeAndDerivedRoundedMinutes() {
        Stoppage value = stoppage(List.of(), Duration.ofSeconds(90));
        assertThat(value.startedAt()).isEqualTo(START);
        assertThat(value.endedAt()).isEqualTo(START.plusSeconds(90));
        assertThat(value.roundedMinutes()).isEqualTo(2);
    }

    @Test
    void largestRemainderMakesFullyAllocatedCansExactAndDeterministic() {
        Stoppage value = stoppage(List.of(), Duration.ofMinutes(10)).withLostCans(5)
                .addExplanation(LossCategory.MATERIAL, "first", 5)
                .addExplanation(LossCategory.QUALITY, "second", 5);
        assertThat(value.explanations()).extracting(LossExplanation::allocatedCans)
                .containsExactly(3, 2);
        assertThat(value.explanations().stream().mapToInt(LossExplanation::allocatedCans).sum()).isEqualTo(5);
    }

    @Test
    void systemShrinkRebalancesCansButPreservesOperatorMinutes() {
        Stoppage value = stoppage(List.of(), Duration.ofMinutes(10)).withLostCans(5)
                .addExplanation(LossCategory.MATERIAL, "first", 5)
                .addExplanation(LossCategory.QUALITY, "second", 5)
                .withSystemMeasurement(START, Duration.ofMinutes(5), 3);
        assertThat(value.explanationStatus()).isEqualTo(ExplanationStatus.ALLOCATION_CONFLICT);
        assertThat(value.explanations()).extracting(LossExplanation::allocatedMinutes).containsExactly(5, 5);
        assertThat(value.explanations()).extracting(LossExplanation::allocatedCans).containsExactly(2, 1);
    }

    private Stoppage stoppage(List<LossExplanation> explanations, Duration duration) {
        return new Stoppage(10L, UUID.fromString("00000000-0000-0000-0000-000000000010"), 1L,
                Stoppage.PRIMARY_SENSOR, 0, START, duration, Stoppage.roundHalfUpMinutes(duration), 100,
                DetectionType.FIXED, StoppageState.ACTIVE, explanations, 0L);
    }
}
