package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;

public record LossExplanationResponse(
        Long id,
        long stoppageId,
        LossCategory category,
        String comment,
        int allocatedMinutes,
        int allocatedCans
) {
    public static LossExplanationResponse from(LossExplanation explanation) {
        return new LossExplanationResponse(explanation.id(), explanation.stoppageId(), explanation.category(),
                explanation.comment(), explanation.allocatedMinutes(), explanation.allocatedCans());
    }
}
