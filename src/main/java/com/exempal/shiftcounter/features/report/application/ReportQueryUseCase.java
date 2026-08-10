package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportQueryUseCase {
    private final StoppageRepository repository;
    private final ProductionDayService productionDays;

    public ReportQueryUseCase(StoppageRepository repository, ProductionDayService productionDays) {
        this.repository = repository;
        this.productionDays = productionDays;
    }

    @Transactional(readOnly = true)
    public ReportView query(Map<String, String> params) {
        LocalDate current = productionDays.current().date();
        LocalDate from = parseDate(params.get("from"), current.minusDays(7));
        LocalDate to = parseDate(params.get("to"), current);
        String sensorId = params.getOrDefault("sensorId", SensorCatalog.SENSOR_1);
        SensorCatalog.require(sensorId);
        List<Map<String, Object>> rows = new ArrayList<>();
        int minutes = 0;
        int cans = 0;
        for (var stoppage : repository.findByShiftDateBetweenAndSensorId(from, to, sensorId)) {
            if (stoppage.state() != StoppageState.ACTIVE) continue;
            for (var explanation : stoppage.explanations()) {
                Map<String, Object> row = new HashMap<>();
                row.put("minutes", explanation.allocatedMinutes());
                row.put("cans", explanation.allocatedCans());
                row.put("type", explanation.category());
                row.put("reason", explanation.comment());
                rows.add(row);
                minutes += explanation.allocatedMinutes();
                cans += explanation.allocatedCans();
            }
        }
        return new ReportView(rows, from, to, sensorId, minutes, cans);
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public record ReportView(List<Map<String, Object>> rows, LocalDate from, LocalDate to,
                             String sensorId, int totalMinutes, int totalCans) {
    }
}
