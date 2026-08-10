package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LossExplanationService implements LossExplanationUseCase {
    private final StoppageRepository stoppages;
    private final LossExplanationRepository explanations;

    @Override
    @Transactional(readOnly = true)
    public List<LossExplanation> findByStoppage(long stoppageId) {
        requireSystemLoss(stoppageId);
        return explanations.findByStoppageId(stoppageId);
    }

    @Override
    @Transactional
    public LossExplanation create(long stoppageId, LossCategory category, String comment, int allocatedMinutes) {
        StoppageEntry stoppage = requireSystemLoss(stoppageId);
        validateAllocation(stoppage, allocatedMinutes, null);
        return explanations.save(new LossExplanation(null, stoppageId, category, comment, allocatedMinutes,
                allocatedCans(stoppage, allocatedMinutes)));
    }

    @Override
    @Transactional
    public LossExplanation update(long stoppageId, long explanationId, LossCategory category,
                                  String comment, int allocatedMinutes) {
        StoppageEntry stoppage = requireSystemLoss(stoppageId);
        LossExplanation existing = requireExplanation(stoppageId, explanationId);
        validateAllocation(stoppage, allocatedMinutes, existing.id());
        return explanations.save(new LossExplanation(existing.id(), stoppageId, category, comment, allocatedMinutes,
                allocatedCans(stoppage, allocatedMinutes)));
    }

    @Override
    @Transactional
    public void delete(long stoppageId, long explanationId) {
        requireSystemLoss(stoppageId);
        explanations.delete(requireExplanation(stoppageId, explanationId));
    }

    private StoppageEntry requireSystemLoss(long stoppageId) {
        StoppageEntry stoppage = stoppages.findById(stoppageId)
                .orElseThrow(() -> new LossExplanationNotFoundException("stoppage " + stoppageId + " not found"));
        if (stoppage.getType() != StoppageType.FIXED && stoppage.getType() != StoppageType.TEMPO) {
            throw new LossAllocationException("explanations can only be attached to FIXED or TEMPO losses");
        }
        return stoppage;
    }

    private LossExplanation requireExplanation(long stoppageId, long explanationId) {
        LossExplanation explanation = explanations.findById(explanationId)
                .orElseThrow(() -> new LossExplanationNotFoundException("explanation " + explanationId + " not found"));
        if (explanation.stoppageId() != stoppageId) {
            throw new LossExplanationNotFoundException("explanation does not belong to stoppage " + stoppageId);
        }
        return explanation;
    }

    private void validateAllocation(StoppageEntry stoppage, int requestedMinutes, Long replacedId) {
        if (requestedMinutes < 0) throw new LossAllocationException("allocatedMinutes must not be negative");
        long roundedMinutes = Math.round(stoppage.getMinutes());
        long alreadyAllocated = explanations.findByStoppageId(stoppage.getId()).stream()
                .filter(e -> replacedId == null || !replacedId.equals(e.id()))
                .mapToLong(LossExplanation::allocatedMinutes)
                .sum();
        if (alreadyAllocated + requestedMinutes > roundedMinutes) {
            throw new LossAllocationException("allocated minutes exceed stoppage rounded minutes");
        }
    }

    private int allocatedCans(StoppageEntry stoppage, int allocatedMinutes) {
        long roundedMinutes = Math.round(stoppage.getMinutes());
        if (roundedMinutes == 0 || allocatedMinutes == 0 || stoppage.getCans() <= 0) return 0;
        return (int) Math.round((double) stoppage.getCans() * allocatedMinutes / roundedMinutes);
    }
}
