package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsReminderService {

    private final StoppageTimeService stoppageTimeService;

    public CommentsReminderService(StoppageTimeService stoppageTimeService) {
        this.stoppageTimeService = stoppageTimeService;
    }

    public List<String> getMissingExplanations(
            Shift shift,
            List<StoppageEntry> entries
    ) {
        List<String> alerts = new ArrayList<>();

        for (StoppageEntry entry : entries) {
            if (entry.getType() != null && entry.getType().isUserEditable()) {
                if (entry.getComment() == null || entry.getComment().isBlank()) {
                    String time = stoppageTimeService.getPreciseTime(entry, shift);
                    alerts.add("Missing explanation for stoppage at " + time + " — please add a comment.");
                }
            }
        }

        return alerts;
    }
    public List<StoppageEntry> getMissingEntries(
            Shift shift,
            List<StoppageEntry> entries
    ) {
        List<StoppageEntry> missing = new ArrayList<>();

        for (StoppageEntry entry : entries) {
            if (entry.getComment() == null || entry.getComment().isBlank()) {
                missing.add(entry);
            }
        }

        return missing;
    }
}