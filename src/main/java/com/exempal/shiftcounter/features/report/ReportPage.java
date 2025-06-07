package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.StoppageEntry;
import com.exempal.shiftcounter.features.comment.StoppageRepository;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReportPage implements PageModel {

    private final StoppageRepository repository;

    public ReportPage(StoppageRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getPageName() {
        return "report";
    }

    @Override
    public void populateModel(Model model) {
        // не используется (для совместимости)
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        if (params.containsKey("from")) {
            try {
                start = LocalDate.parse(params.get("from"), formatter);
            } catch (Exception ignored) {}
        }

        if (params.containsKey("to")) {
            try {
                end = LocalDate.parse(params.get("to"), formatter);
            } catch (Exception ignored) {}
        }

        List<StoppageEntry> entries = repository.findByDateBetween(start, end);

        List<Map<String, Object>> problems = new ArrayList<>();
        int totalMinutes = 0;
        int totalCans = 0;

        for (StoppageEntry e : entries) {
            Map<String, Object> row = new HashMap<>();
            row.put("minutes", e.getMinutes());
            row.put("cans", e.getCans());
            row.put("type", e.getType());
            row.put("reason", e.getComment());

            problems.add(row);

            totalMinutes += e.getMinutes();
            totalCans += e.getCans();
        }

        model.addAttribute("problems", problems);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("totalCans", totalCans);
    }
}
