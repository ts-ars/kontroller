package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;

import java.time.LocalDate;

public record ReportRow(
        String source,
        String type,
        int minutes,
        int cans,
        String reason,
        String author,
        LocalDate productionDate,
        Long stoppageId
) {
    public ReportRow(String source, LossCategory type, int minutes, int cans, String reason) {
        this(source, type.name(), minutes, cans, reason, "", LocalDate.MIN, null);
    }

    public ReportRow(String source, LossCategory type, int minutes, int cans, String reason,
                     String author, LocalDate productionDate) {
        this(source, type.name(), minutes, cans, reason, author, productionDate, null);
    }

    public ReportRow(String source, String type, int minutes, int cans, String reason,
                     String author, LocalDate productionDate) {
        this(source, type, minutes, cans, reason, author, productionDate, null);
    }
}
