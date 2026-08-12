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
import java.util.Comparator;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;

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
                            explanation.allocatedCans(), explanation.comment(), explanation.authorDisplayName(),
                            productionDays.resolve(stoppage.startedAt()).date()));
                    minutes += explanation.allocatedMinutes();
                    cans += explanation.allocatedCans();
                    lostCansBySource.merge(source, explanation.allocatedCans(), Integer::sum);
                }
            }
        }
        List<ReportLossTotal> lossTotals = lostCansBySource.entrySet().stream()
                .map(entry -> new ReportLossTotal(entry.getKey(), entry.getValue()))
                .toList();
        rows.sort(Comparator.comparingInt(ReportRow::cans).reversed()
                .thenComparing(Comparator.comparingInt(ReportRow::minutes).reversed()));
        String grouping = grouping(from, to);
        List<ReportTimeTotal> timeTotals = SensorCatalog.SENSOR_5.equals(sensorId)
                ? List.of()
                : timeTotals(rows, from, to, grouping);
        return new ReportView(rows, from, to, sensorId, minutes, cans, lossTotals, timeTotals, grouping);
    }

    private String grouping(LocalDate from, LocalDate to) {
        long days = Math.max(1, ChronoUnit.DAYS.between(from, to) + 1);
        if (days <= 7) return "daily";
        if (days <= 31) return "weekly";
        return "monthly";
    }

    private List<ReportTimeTotal> timeTotals(List<ReportRow> rows, LocalDate from, LocalDate to,
                                                   String grouping) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        if ("daily".equals(grouping)) {
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                totals.put(date.toString(), 0);
            }
        } else if ("weekly".equals(grouping)) {
            for (LocalDate start = from; !start.isAfter(to); start = start.plusWeeks(1)) {
                LocalDate end = start.plusDays(6).isAfter(to) ? to : start.plusDays(6);
                totals.put(start + "\u2013" + end, 0);
            }
        } else {
            for (YearMonth month = YearMonth.from(from); !month.isAfter(YearMonth.from(to)); month = month.plusMonths(1)) {
                totals.put(month.toString(), 0);
            }
        }
        for (ReportRow row : rows) {
            String key;
            if ("daily".equals(grouping)) {
                key = row.productionDate().toString();
            } else if ("weekly".equals(grouping)) {
                long offset = Math.max(0, ChronoUnit.DAYS.between(from, row.productionDate()));
                LocalDate start = from.plusWeeks(offset / 7);
                LocalDate end = start.plusDays(6).isAfter(to) ? to : start.plusDays(6);
                key = start + "\u2013" + end;
            } else {
                key = YearMonth.from(row.productionDate()).toString();
            }
            if (totals.containsKey(key)) totals.merge(key, row.cans(), Integer::sum);
        }
        return totals.entrySet().stream().map(value -> new ReportTimeTotal(value.getKey(), value.getValue())).toList();
    }

    private LocalDate parseDate(String raw, LocalDate fallback) {
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
