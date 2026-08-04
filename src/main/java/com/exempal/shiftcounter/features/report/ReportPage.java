package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
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
        // Не используется — вызовется перегруженный метод с params
    }

    @Override
    public void populateModel(Model model, Map<String, String> params) {
        LocalDate from = parseDateParam(params.get("from"), LocalDate.now().minusDays(7));
        LocalDate to = parseDateParam(params.get("to"), LocalDate.now());

        List<StoppageEntry> entries = repository.findByShiftDateBetween(from, to).stream()
                .filter(entry -> entry.getType() != null && entry.getType().isUserEditable())
                .toList();

        List<Map<String, Object>> problems = new ArrayList<>();
        int totalMinutes = 0;
        int totalCans = 0;

        for (StoppageEntry entry : entries) {
            Map<String, Object> row = new HashMap<>();
            row.put("minutes", entry.getMinutes());
            row.put("cans", entry.getCans());
            row.put("type", entry.getType());
            row.put("reason", entry.getComment());

            problems.add(row);
            totalMinutes += entry.getMinutes();
            totalCans += entry.getCans();
        }

        model.addAttribute("problems", problems);
        model.addAttribute("startDate", from.toString());
        model.addAttribute("endDate", to.toString());
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("totalCans", totalCans);
    }

    private LocalDate parseDateParam(String raw, LocalDate fallback) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            return fallback;
        }
    }
}
