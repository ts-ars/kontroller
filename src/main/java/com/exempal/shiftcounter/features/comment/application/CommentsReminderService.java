package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentsReminderService {
    private final StoppageTimeService timeService;

    public CommentsReminderService(StoppageTimeService timeService) {
        this.timeService = timeService;
    }

    public List<String> getMissingExplanations(List<Stoppage> stoppages) {
        return stoppages.stream()
                .filter(value -> value.explanationStatus() == ExplanationStatus.UNEXPLAINED)
                .map(value -> "Missing explanation for stoppage at " + timeService.format(value))
                .toList();
    }

    public List<Stoppage> getMissingEntries(List<Stoppage> stoppages) {
        return stoppages.stream()
                .filter(value -> value.explanationStatus() == ExplanationStatus.UNEXPLAINED)
                .toList();
    }
}
