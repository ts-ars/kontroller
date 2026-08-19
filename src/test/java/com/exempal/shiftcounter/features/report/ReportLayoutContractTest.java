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
                .contains(".feature-page")
                .contains("max-width: 1632px")
                .contains("main { container-type: inline-size; }")
                .contains(".report-filters input[type=\"date\"]")
                .contains("width: 130px")
                .contains(".report-charts")
                .contains("width: 100%")
                .contains("grid-template-columns: repeat(4, minmax(0, 1fr))")
                .contains(".report-filter-heading")
                .contains(".report-filter-icon::before")
                .contains(".report-chart-card canvas")
                .contains("#plan-fact-page .chart-frame canvas")
                .contains("width: 100% !important")
                .contains("height: 190px !important");
        assertThat(css).doesNotContain("overflow-x", "min-width: 900px", "100vw");
    }
}
