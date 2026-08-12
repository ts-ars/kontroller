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
        List<ReportSignalTotal> signalTotals
) {
    public ReportView {
        rows = List.copyOf(rows);
        signalTotals = List.copyOf(signalTotals);
    }
}
