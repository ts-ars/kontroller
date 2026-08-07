package com.exempal.shiftcounter.features.comment.domain;

public record LossExplanation(
        Long id,
        long stoppageId,
        LossCategory category,
        String comment,
        int allocatedMinutes,
        int allocatedCans
) {
    public LossExplanation {
        if (stoppageId <= 0) throw new IllegalArgumentException("stoppageId must be positive");
        if (category == null) throw new IllegalArgumentException("category is required");
        if (allocatedMinutes < 0) throw new IllegalArgumentException("allocatedMinutes must not be negative");
        if (allocatedCans < 0) throw new IllegalArgumentException("allocatedCans must not be negative");
        comment = comment == null ? "" : comment.trim();
    }
}
