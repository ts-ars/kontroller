package com.exempal.shiftcounter.features.comment.domain;

import java.time.Instant;
import java.util.UUID;

public record LossExplanation(
        Long id,
        long stoppageId,
        LossCategory category,
        String comment,
        int allocatedMinutes,
        int allocatedCans,
        UUID authorUserId,
        String authorDisplayName,
        Instant createdAt,
        Instant updatedAt,
        UUID lastModifiedBy,
        long version
) {
    public LossExplanation(Long id, long stoppageId, LossCategory category, String comment,
                           int allocatedMinutes, int allocatedCans) {
        this(id, stoppageId, category, comment, allocatedMinutes, allocatedCans,
                null, "", null, null, null, 0L);
    }

    public LossExplanation(Long id, long stoppageId, LossCategory category, String comment,
                           int allocatedMinutes, int allocatedCans, long version) {
        this(id, stoppageId, category, comment, allocatedMinutes, allocatedCans,
                null, "", null, null, null, version);
    }

    public LossExplanation {
        if (stoppageId <= 0) throw new IllegalArgumentException("stoppageId must be positive");
        if (category == null) throw new IllegalArgumentException("category is required");
        if (allocatedMinutes < 0) throw new IllegalArgumentException("allocatedMinutes must not be negative");
        if (allocatedCans < 0) throw new IllegalArgumentException("allocatedCans must not be negative");
        if (version < 0) throw new IllegalArgumentException("version must not be negative");
        comment = comment == null ? "" : comment.trim();
        authorDisplayName = authorDisplayName == null ? "" : authorDisplayName.trim();
    }
}
