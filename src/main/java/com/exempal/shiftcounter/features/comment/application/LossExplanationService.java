package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LossExplanationService implements LossExplanationUseCase {
    private final StoppageRepository stoppages;

    @Override
    @Transactional(readOnly = true)
    public List<LossExplanation> findByStoppage(long stoppageId) {
        return requireSystemLoss(stoppageId, false).explanations();
    }

    @Override
    @Transactional
    public LossExplanation create(long stoppageId, LossCategory category, String comment, int allocatedMinutes) {
        Stoppage stoppage = requireSystemLoss(stoppageId, true);
        try {
            Stoppage saved = stoppages.save(stoppage.addExplanation(category, comment, allocatedMinutes));
            return saved.explanations().getLast();
        } catch (IllegalArgumentException exception) {
            throw new LossAllocationException(exception.getMessage());
        }
    }

    @Override
    @Transactional
    public LossExplanation update(long stoppageId, long explanationId, LossCategory category,
                                  String comment, int allocatedMinutes) {
        Stoppage stoppage = requireSystemLoss(stoppageId, true);
        try {
            Stoppage saved = stoppages.save(
                    stoppage.updateExplanation(explanationId, category, comment, allocatedMinutes));
            return saved.explanations().stream().filter(value -> value.id() == explanationId).findFirst()
                    .orElseThrow(() -> new LossExplanationNotFoundException(
                            "explanation " + explanationId + " not found"));
        } catch (IllegalArgumentException exception) {
            if ("explanation not found".equals(exception.getMessage())) {
                throw new LossExplanationNotFoundException("explanation " + explanationId + " not found");
            }
            throw new LossAllocationException(exception.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(long stoppageId, long explanationId) {
        Stoppage stoppage = requireSystemLoss(stoppageId, true);
        try {
            stoppages.save(stoppage.removeExplanation(explanationId));
        } catch (IllegalArgumentException exception) {
            throw new LossExplanationNotFoundException("explanation " + explanationId + " not found");
        }
    }

    private Stoppage requireSystemLoss(long stoppageId, boolean forUpdate) {
        Stoppage stoppage = (forUpdate ? stoppages.findForUpdateById(stoppageId) : stoppages.findById(stoppageId))
                .orElseThrow(() -> new LossExplanationNotFoundException("stoppage " + stoppageId + " not found"));
        if (SensorCatalog.SENSOR_5.equals(stoppage.sensorKey())) {
            throw new LossAllocationException("Sensor 5 has no stoppage explanation workflow");
        }
        return stoppage;
    }
}
