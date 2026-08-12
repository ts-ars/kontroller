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
        String timeGrouping
) {
    public ReportView(List<ReportRow> rows, LocalDate from, LocalDate to, String sensorId,
                      int totalMinutes, int totalCans, List<ReportLossTotal> lossTotals) {
        this(rows, from, to, sensorId, totalMinutes, totalCans, lossTotals, List.of(), "daily");
    }

    public ReportView {
        rows = List.copyOf(rows);
        lossTotals = List.copyOf(lossTotals);
        timeTotals = List.copyOf(timeTotals);
    }
}
