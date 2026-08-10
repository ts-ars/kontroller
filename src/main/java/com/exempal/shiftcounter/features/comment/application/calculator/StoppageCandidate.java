package com.exempal.shiftcounter.features.comment.application.calculator;

import com.exempal.shiftcounter.features.comment.domain.DetectionType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record StoppageCandidate(
        DetectionType detectionType,
        LocalDateTime startedAt,
        Duration exactDuration,
        int lostCans
) {
    public StoppageCandidate {
        Objects.requireNonNull(detectionType, "detectionType");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(exactDuration, "exactDuration");
        if (exactDuration.isNegative()) throw new IllegalArgumentException("exactDuration must not be negative");
        if (lostCans < 0) throw new IllegalArgumentException("lostCans must not be negative");
    }

    public LocalDateTime endedAt() {
        return startedAt.plus(exactDuration);
    }

    public StoppageCandidate withLostCans(int cans) {
        return new StoppageCandidate(detectionType, startedAt, exactDuration, cans);
    }
}
