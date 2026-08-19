package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.domain.Shift;
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
import java.util.Locale;

@Service
public class ReportQueryUseCase {
    private final StoppageRepository repository;
    private final ProductionDayService productionDays;
    private final ActualDataPort shifts;
    private final ShiftIntervalService intervals;

    public ReportQueryUseCase(StoppageRepository repository, ProductionDayService productionDays,
                              ActualDataPort shifts, ShiftIntervalService intervals) {
        this.repository = repository;
        this.productionDays = productionDays;
        this.shifts = shifts;
        this.intervals = intervals;
    }

    @Transactional(readOnly = true)
    public ReportView query(Map<String, String> params) {
        LocalDate current = productionDays.current().date();
        LocalDate from = parseDate(params.get("from"), current);
        LocalDate to = parseDate(params.get("to"), current);
        String sensorId = params.getOrDefault("sensorId", SensorCatalog.SENSOR_5);
        SensorCatalog.require(sensorId);
        List<ReportRow> rows = new ArrayList<>();
        int minutes = 0;
        int cans = 0;
        List<String> explanationSources = SensorCatalog.SENSOR_5.equals(sensorId)
                ? List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4")
                : List.of(sensorId);
        String requestedSource = params.getOrDefault("source", "");
        String sourceFilter = explanationSources.contains(requestedSource) ? requestedSource : "";
        LossCategory typeFilter = parseType(params.get("type"));
        String reasonFilter = normalize(params.get("reason"));
        String authorFilter = normalize(params.get("author"));
        Map<String, Integer> lostCansBySource = new LinkedHashMap<>();
        Map<LocalDate, Map<Integer, Integer>> explainedCansByInterval = new LinkedHashMap<>();
        explanationSources.forEach(source -> lostCansBySource.put(source, 0));
        for (String source : explanationSources) {
            for (var stoppage : repository.findByShiftDateBetweenAndSensorId(from, to, source)) {
                LocalDate stoppageDate = productionDays.resolve(stoppage.startedAt()).date();
                int explainedMinutes = 0;
                int explainedCans = 0;
                for (var explanation : stoppage.explanations()) {
                    explainedMinutes += explanation.allocatedMinutes();
                    explainedCans += explanation.allocatedCans();
                    if (sourceFilter.isEmpty() || sourceFilter.equals(source)) {
                        explainedCansByInterval.computeIfAbsent(stoppageDate, ignored -> new LinkedHashMap<>())
                                .merge(stoppage.intervalIndex(), explanation.allocatedCans(), Integer::sum);
                    }
                    if (matches(source, explanation.category(), explanation.comment(),
                            explanation.authorDisplayName(), sourceFilter, typeFilter, reasonFilter, authorFilter)) {
                        rows.add(new ReportRow(source, explanation.category().name(), explanation.allocatedMinutes(),
                                explanation.allocatedCans(), explanation.comment(), explanation.authorDisplayName(),
                                stoppageDate, stoppage.id()));
                        minutes += explanation.allocatedMinutes();
                        cans += explanation.allocatedCans();
                        lostCansBySource.merge(source, explanation.allocatedCans(), Integer::sum);
                    }
                }
                if (!stoppage.endedAt().isAfter(productionDays.now())) {
                    int remainingMinutes = Math.max(0, stoppage.roundedMinutes() - explainedMinutes);
                    int remainingCans = Math.max(0, stoppage.lostCans() - explainedCans);
                    if (explainedMinutes > stoppage.roundedMinutes()) {
                        rows.add(new ReportRow(source, "ALLOCATION_CONFLICT", 0, 0,
                                "Allocated " + explainedMinutes + " min exceeds stoppage "
                                        + stoppage.roundedMinutes() + " min", "",
                                stoppageDate, stoppage.id()));
                    } else if (remainingMinutes > 0 || remainingCans > 0) {
                        rows.add(new ReportRow(source, "UNEXPLAINED", remainingMinutes, remainingCans,
                                "No explanation provided", "",
                                stoppageDate, stoppage.id()));
                        minutes += remainingMinutes;
                        cans += remainingCans;
                        lostCansBySource.merge(source, remainingCans, Integer::sum);
                    }
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
        String chartSensorId = sourceFilter.isEmpty() ? sensorId : sourceFilter;
        List<Shift> reportShifts = shifts(from, to, chartSensorId);
        List<ReportChartPoint> productionTotals = chartTotals(
                reportShifts, from, to, grouping, true, Map.of());
        List<ReportChartPoint> unexplainedTotals = chartTotals(
                reportShifts, from, to, grouping, false, explainedCansByInterval);
        int totalProduction = productionTotals.stream().mapToInt(ReportChartPoint::value).sum();
        return new ReportView(rows, from, to, sensorId, minutes, cans, lossTotals, timeTotals, grouping,
                productionTotals, totalProduction, unexplainedTotals);
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

    private List<Shift> shifts(LocalDate from, LocalDate to, String sensorId) {
        List<Shift> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            shifts.findByDateAndSensorId(date, sensorId).ifPresent(result::add);
        }
        return result;
    }

    private List<ReportChartPoint> chartTotals(List<Shift> values, LocalDate from, LocalDate to,
                                               String grouping, boolean production,
                                               Map<LocalDate, Map<Integer, Integer>> explainedCansByInterval) {
        if (from.equals(to)) {
            return values.stream().findFirst().map(shift -> intervalTotals(shift, production,
                    explainedCansByInterval.getOrDefault(shift.getDate(), Map.of()))).orElse(List.of());
        }
        Map<String, Integer> totals = new LinkedHashMap<>();
        if ("daily".equals(grouping)) {
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) totals.put(date.toString(), 0);
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
        for (Shift shift : values) {
            String key = bucket(shift.getDate(), from, to, grouping);
            totals.merge(key, production ? shift.getActual() : unexplained(shift,
                    explainedCansByInterval.getOrDefault(shift.getDate(), Map.of())), Integer::sum);
        }
        return totals.entrySet().stream().map(entry -> new ReportChartPoint(entry.getKey(), entry.getValue())).toList();
    }

    private List<ReportChartPoint> intervalTotals(Shift shift, boolean production,
                                                  Map<Integer, Integer> explainedCansByInterval) {
        List<ReportChartPoint> result = new ArrayList<>();
        var resolvedIntervals = intervals.resolve(shift.getDate(), shift.getHourlyLabels(),
                shift.getHourlyPlanValues().size());
        for (var interval : resolvedIntervals) {
            int index = interval.index();
            int actual = index < shift.getHourlyActualValues().size() ? shift.getHourlyActualValues().get(index) : 0;
            int plan = index < shift.getHourlyPlanValues().size() ? shift.getHourlyPlanValues().get(index) : 0;
            result.add(new ReportChartPoint(shift.getHourlyLabels().get(index),
                    production ? actual : interval.end().isAfter(productionDays.now()) ? 0 : Math.max(0,
                            plan - actual - explainedCansByInterval.getOrDefault(index, 0))));
        }
        return List.copyOf(result);
    }

    private int unexplained(Shift shift, Map<Integer, Integer> explainedCansByInterval) {
        int total = 0;
        for (var interval : intervals.resolve(shift.getDate(), shift.getHourlyLabels(),
                shift.getHourlyPlanValues().size())) {
            if (interval.end().isAfter(productionDays.now())) continue;
            int index = interval.index();
            int actual = index < shift.getHourlyActualValues().size() ? shift.getHourlyActualValues().get(index) : 0;
            total += Math.max(0, shift.getHourlyPlanValues().get(index) - actual
                    - explainedCansByInterval.getOrDefault(index, 0));
        }
        return total;
    }

    private String bucket(LocalDate date, LocalDate from, LocalDate to, String grouping) {
        if ("daily".equals(grouping)) return date.toString();
        if ("monthly".equals(grouping)) return YearMonth.from(date).toString();
        long offset = Math.max(0, ChronoUnit.DAYS.between(from, date));
        LocalDate start = from.plusWeeks(offset / 7);
        LocalDate end = start.plusDays(6).isAfter(to) ? to : start.plusDays(6);
        return start + "\u2013" + end;
    }

    private LossCategory parseType(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : LossCategory.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean contains(String value, String filter) {
        return filter.isEmpty() || value != null && value.toLowerCase(Locale.ROOT).contains(filter);
    }

    private boolean matches(String source, LossCategory category, String reason, String author,
                            String sourceFilter, LossCategory typeFilter, String reasonFilter, String authorFilter) {
        return (sourceFilter.isEmpty() || sourceFilter.equals(source))
                && (typeFilter == null || typeFilter == category)
                && contains(reason, reasonFilter)
                && contains(author, authorFilter);
    }
}
