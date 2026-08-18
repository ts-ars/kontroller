package com.exempal.shiftcounter.features.report.application;

import java.time.LocalDate;
import java.util.List;

public record ReportView(
        List<ReportRow> rows,
        LocalDate from,
        LocalDate to,
        String sensorId,
        int totalMinutes,
        int totalCans,
        List<ReportLossTotal> lossTotals,
        List<ReportTimeTotal> timeTotals,
        String timeGrouping,
        List<ReportChartPoint> productionTotals,
        int totalProduction,
        List<ReportChartPoint> unexplainedPlanTotals
) {
    public ReportView(List<ReportRow> rows, LocalDate from, LocalDate to, String sensorId,
                      int totalMinutes, int totalCans, List<ReportLossTotal> lossTotals) {
        this(rows, from, to, sensorId, totalMinutes, totalCans, lossTotals, List.of(), "daily",
                List.of(), 0, List.of());
    }

    public ReportView(List<ReportRow> rows, LocalDate from, LocalDate to, String sensorId,
                      int totalMinutes, int totalCans, List<ReportLossTotal> lossTotals,
                      List<ReportTimeTotal> timeTotals, String timeGrouping) {
        this(rows, from, to, sensorId, totalMinutes, totalCans, lossTotals, timeTotals, timeGrouping,
                List.of(), 0, List.of());
    }

    public ReportView {
        rows = List.copyOf(rows);
        lossTotals = List.copyOf(lossTotals);
        timeTotals = List.copyOf(timeTotals);
        productionTotals = List.copyOf(productionTotals);
        unexplainedPlanTotals = List.copyOf(unexplainedPlanTotals);
    }
}
