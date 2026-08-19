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
                .contains("--operational-chart-height: 214px")
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
                .contains("height: var(--operational-chart-height) !important")
                .contains(".feature-page { --operational-chart-height: 200px; }");
        assertThat(css).doesNotContain("overflow-x", "min-width: 900px", "100vw");
    }

    @Test
    void reportValueLabelsAvoidCollisionsWithoutHidingPositiveValues() throws Exception {
        String template = Files.readString(Path.of(
                "src/main/resources/templates/features/report/report.html"));

        assertThat(template)
                .contains("ctx.measureText(text).width/2")
                .contains("occupied.some(other=>")
                .contains("bar.base-bar.y>=22?bar.y+14:bar.y-19")
                .contains("if(value<=0)return");
    }
}
