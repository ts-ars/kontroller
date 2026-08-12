package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;

public record ReportRow(
        String source,
        LossCategory type,
        int minutes,
        int cans,
        String reason
) {
}
