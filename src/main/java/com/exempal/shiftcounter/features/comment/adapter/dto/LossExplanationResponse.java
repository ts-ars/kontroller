package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import java.time.Instant;
import java.util.UUID;

public record LossExplanationResponse(
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
    public static LossExplanationResponse from(LossExplanation explanation) {
        return new LossExplanationResponse(explanation.id(), explanation.stoppageId(), explanation.category(),
                explanation.comment(), explanation.allocatedMinutes(), explanation.allocatedCans(),
                explanation.authorUserId(), explanation.authorDisplayName(), explanation.createdAt(),
                explanation.updatedAt(), explanation.lastModifiedBy(), explanation.version());
    }
}
