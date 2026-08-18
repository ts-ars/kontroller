package com.exempal.shiftcounter.features.report.adapter;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReportLayoutContractTest {

    @Test
    void keepsApprovedCompactReportAndPlanFactChartDimensions() throws Exception {
        String css = Files.readString(Path.of("src/main/resources/static/css/styles.css"));

        assertThat(css)
                .contains(".report-page")
                .contains("width: min(100%, 1400px)")
                .contains(".report-filters input[type=\"date\"]")
                .contains("width: 130px")
                .contains(".report-charts")
                .contains("width: calc(100vw - 120px)")
                .contains("grid-template-columns: repeat(4, minmax(0, 1fr))")
                .contains(".report-filter-heading")
                .contains(".report-filter-icon::before")
                .contains(".report-chart-card canvas")
                .contains("#plan-fact-page .chart-frame canvas")
                .contains("width: 100% !important")
                .contains("height: 190px !important");
    }
}
