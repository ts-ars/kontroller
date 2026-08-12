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
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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
        List<ReportRow> rows = new ArrayList<>();
        int minutes = 0;
        int cans = 0;
        List<String> explanationSources = SensorCatalog.SENSOR_5.equals(sensorId)
                ? List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4")
                : List.of(sensorId);
        Map<String, Integer> lostCansBySource = new LinkedHashMap<>();
        explanationSources.forEach(source -> lostCansBySource.put(source, 0));
        for (String source : explanationSources) {
            for (var stoppage : repository.findByShiftDateBetweenAndSensorId(from, to, source)) {
                if (stoppage.state() != StoppageState.ACTIVE) continue;
                for (var explanation : stoppage.explanations()) {
                    rows.add(new ReportRow(source, explanation.category(), explanation.allocatedMinutes(),
                            explanation.allocatedCans(), explanation.comment()));
                    minutes += explanation.allocatedMinutes();
                    cans += explanation.allocatedCans();
                    lostCansBySource.merge(source, explanation.allocatedCans(), Integer::sum);
                }
            }
        }
        List<ReportLossTotal> lossTotals = lostCansBySource.entrySet().stream()
                .map(entry -> new ReportLossTotal(entry.getKey(), entry.getValue()))
                .toList();
        return new ReportView(rows, from, to, sensorId, minutes, cans, lossTotals);
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
