package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.core.PageModel;
import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ReportPage implements PageModel {

    private final StoppageRepository repository;
    private final ProductionDayService productionDays;

    public ReportPage(StoppageRepository repository, ProductionDayService productionDays) {
        this.repository = repository;
        this.productionDays = productionDays;
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
        LocalDate current = productionDays.current().date();
        LocalDate from = parseDateParam(params.get("from"), current.minusDays(7));
        LocalDate to = parseDateParam(params.get("to"), current);
        String sensorId = params.getOrDefault("sensorId", "sensor-1");
        com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.require(sensorId);

        List<Stoppage> entries = repository.findByShiftDateBetweenAndSensorId(from, to, sensorId).stream()
                .filter(entry -> entry.state() == StoppageState.ACTIVE)
                .toList();

        List<Map<String, Object>> problems = new ArrayList<>();
        int totalMinutes = 0;
        int totalCans = 0;

        for (Stoppage entry : entries) {
            for (var explanation : entry.explanations()) {
                Map<String, Object> row = new HashMap<>();
                row.put("minutes", explanation.allocatedMinutes());
                row.put("cans", explanation.allocatedCans());
                row.put("type", explanation.category());
                row.put("reason", explanation.comment());

                problems.add(row);
                totalMinutes += explanation.allocatedMinutes();
                totalCans += explanation.allocatedCans();
            }
        }

        model.addAttribute("problems", problems);
        model.addAttribute("startDate", from.toString());
        model.addAttribute("endDate", to.toString());
        model.addAttribute("sensorId", sensorId);
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
