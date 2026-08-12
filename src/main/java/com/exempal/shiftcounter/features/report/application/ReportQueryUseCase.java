package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.domain.ProductionDay;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportQueryUseCase {
    private final StoppageRepository repository;
    private final ProductionDayService productionDays;
    private final ReportSignalQueryPort signals;

    public ReportQueryUseCase(StoppageRepository repository, ProductionDayService productionDays,
                              ReportSignalQueryPort signals) {
        this.repository = repository;
        this.productionDays = productionDays;
        this.signals = signals;
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
        for (String source : explanationSources) {
            for (var stoppage : repository.findByShiftDateBetweenAndSensorId(from, to, source)) {
                if (stoppage.state() != StoppageState.ACTIVE) continue;
                for (var explanation : stoppage.explanations()) {
                    rows.add(new ReportRow(source, explanation.category(), explanation.allocatedMinutes(),
                            explanation.allocatedCans(), explanation.comment()));
                    minutes += explanation.allocatedMinutes();
                    cans += explanation.allocatedCans();
                }
            }
        }
        var rangeStart = ProductionDay.of(from).start();
        var rangeEnd = ProductionDay.of(to).end();
        List<String> signalSources = SensorCatalog.SENSOR_5.equals(sensorId)
                ? explanationSources
                : List.of(sensorId);
        List<ReportSignalTotal> signalTotals = signalSources.stream()
                .map(source -> new ReportSignalTotal(source, signals.count(source, rangeStart, rangeEnd)))
                .toList();
        return new ReportView(rows, from, to, sensorId, minutes, cans, signalTotals);
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
