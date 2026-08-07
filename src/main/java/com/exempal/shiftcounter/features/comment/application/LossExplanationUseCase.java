package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;

import java.util.List;

public interface LossExplanationUseCase {
    List<LossExplanation> findByStoppage(long stoppageId);
    LossExplanation create(long stoppageId, LossCategory category, String comment, int allocatedMinutes);
    LossExplanation update(long stoppageId, long explanationId, LossCategory category, String comment, int allocatedMinutes);
    void delete(long stoppageId, long explanationId);
}
