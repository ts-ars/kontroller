package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class LossExplanationService implements LossExplanationUseCase {
    private final StoppageRepository stoppages;
    private final ActualDataPort shifts;
    private final EventPublisherPort events;
    private final CurrentCommentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<LossExplanation> findByStoppage(long stoppageId) {
        return requireSystemLoss(stoppageId, false).explanations();
    }

    @Override
    @Transactional
    public LossExplanation create(long stoppageId, LossCategory category, String comment, int allocatedMinutes) {
        Stoppage stoppage = requireSystemLoss(stoppageId, true);
        CommentActor actor = currentActor.require();
        try {
            Stoppage saved = stoppages.save(stoppage.addExplanation(category, comment, allocatedMinutes,
                    actor.userId(), actor.displayName(), clock.instant()));
            publishUpdated(saved);
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
        LossExplanation existing = requireExplanation(stoppage, explanationId);
        CommentActor actor = requireMayModify(existing);
        try {
            Stoppage saved = stoppages.save(
                    stoppage.updateExplanation(explanationId, category, comment, allocatedMinutes,
                            actor.userId(), clock.instant()));
            publishUpdated(saved);
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
        requireMayModify(requireExplanation(stoppage, explanationId));
        try {
            Stoppage saved = stoppages.save(stoppage.removeExplanation(explanationId));
            publishUpdated(saved);
        } catch (IllegalArgumentException exception) {
            throw new LossExplanationNotFoundException("explanation " + explanationId + " not found");
        }
    }

    private LossExplanation requireExplanation(Stoppage stoppage, long explanationId) {
        return stoppage.explanations().stream().filter(value -> value.id() != null && value.id() == explanationId)
                .findFirst().orElseThrow(() -> new LossExplanationNotFoundException(
                        "explanation " + explanationId + " not found"));
    }

    private CommentActor requireMayModify(LossExplanation explanation) {
        CommentActor actor = currentActor.require();
        if (!actor.mayModify(explanation.authorUserId())) throw new CommentAccessDeniedException();
        return actor;
    }

    private Stoppage requireSystemLoss(long stoppageId, boolean forUpdate) {
        Stoppage stoppage = (forUpdate ? stoppages.findForUpdateById(stoppageId) : stoppages.findById(stoppageId))
                .orElseThrow(() -> new LossExplanationNotFoundException("stoppage " + stoppageId + " not found"));
        if (SensorCatalog.SENSOR_5.equals(stoppage.sensorKey())) {
            throw new LossAllocationException("Sensor 5 has no stoppage explanation workflow");
        }
        return stoppage;
    }

    private void publishUpdated(Stoppage stoppage) {
        var shift = shifts.findById(stoppage.shiftId())
                .orElseThrow(() -> new IllegalStateException("shift " + stoppage.shiftId() + " not found"));
        events.publish(new CommentsUpdatedEvent(shift.getDate(), stoppage.sensorKey()));
    }
}
