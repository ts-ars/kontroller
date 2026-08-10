package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.LossExplanation;

import java.util.List;
import java.util.Optional;

public interface LossExplanationRepository {
    List<LossExplanation> findByStoppageId(long stoppageId);
    Optional<LossExplanation> findById(long id);
    LossExplanation save(LossExplanation explanation);
    void delete(LossExplanation explanation);
}
