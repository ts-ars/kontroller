package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.core.PageModel;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommentsPage implements PageModel {

    private final StoppageRepository repository;

    public CommentsPage(StoppageRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getPageName() {
        return "comment";
    }

    @Override
    public void populateModel(Model model) {
        LocalDate today = LocalDate.now();
        List<StoppageEntry> entries = repository.findByDate(today);

        List<String> alerts = new ArrayList<>();
        for (StoppageEntry entry : entries) {
            if (entry.getComment() == null || entry.getComment().isBlank()) {
                alerts.add("Missing explanation for " + entry.getTime() + " — please add a comment.");
            }
        }

        model.addAttribute("rows", entries);
        model.addAttribute("alerts", alerts);
    }
}
