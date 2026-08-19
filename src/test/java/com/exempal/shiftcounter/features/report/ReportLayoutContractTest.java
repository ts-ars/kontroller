package com.exempal.shiftcounter.features.report.adapter;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReportLayoutContractTest {

    @Test
    void keepsApprovedCompactReportAndPlanFactChartDimensions() throws Exception {
        String css = Files.readString(Path.of("src/main/resources/static/css/styles.css"));
        String report = Files.readString(Path.of("src/main/resources/templates/features/report/report.html"));
        String planFact = Files.readString(Path.of("src/main/resources/templates/features/shift/shift.html"));

        assertThat(css)
                .contains(".feature-page")
                .contains("max-width: 1632px")
                .contains("--operational-chart-height: 238px")
                .contains("--operational-chart-lift: 24px")
                .contains("main { container-type: inline-size; }")
                .contains(".report-filters input[type=\"date\"]")
                .contains("width: 130px")
                .contains(".report-charts")
                .contains("width: 100%")
                .contains("grid-template-columns: repeat(4, minmax(0, 1fr))")
                .contains(".report-filter-heading")
                .contains(".report-filter-icon::before")
                .contains(".operational-chart-frame")
                .contains("width: 100% !important")
                .contains("height: 100% !important")
                .contains("--operational-chart-height: 220px")
                .contains("--operational-chart-lift: 20px");
        assertThat(css).doesNotContain("overflow-x", "min-width: 900px", "100vw");
        assertThat(report).contains("class=\"operational-chart-frame\"");
        assertThat(planFact).contains("class=\"chart-frame operational-chart-frame\"")
                .doesNotContain("height=\"190\"");
    }

    @Test
    void reportValueLabelsAvoidCollisionsWithoutHidingPositiveValues() throws Exception {
        String charts = Files.readString(Path.of(
                "src/main/resources/static/js/operational-charts.js"));

        assertThat(charts)
                .contains("ctx.measureText(text).width / 2")
                .contains("occupied.some(other =>")
                .contains("bar.base-bar.y >= 22 ? bar.y+14 : bar.y-19")
                .contains("if (value <= 0) return");
    }

    @Test
    void keepsApprovedReportChartOrder() throws Exception {
        String template = Files.readString(Path.of(
                "src/main/resources/templates/features/report/report.html"));

        assertThat(template.indexOf("id=\"productionChart\""))
                .isLessThan(template.indexOf("id=\"unexplainedChart\""));
        assertThat(template.indexOf("id=\"unexplainedChart\""))
                .isLessThan(template.indexOf("id=\"lossChart\""));
        assertThat(template.indexOf("id=\"lossChart\""))
                .isLessThan(template.indexOf("id=\"cansChart\""));
    }

    @Test
    void centrallyReservesFourStableSlotsForReportCharts() throws Exception {
        String template = Files.readString(Path.of(
                "src/main/resources/templates/features/report/report.html"));
        String charts = Files.readString(Path.of(
                "src/main/resources/static/js/operational-charts.js"));

        assertThat(template)
                .contains("/js/operational-charts.js")
                .contains("OperationalCharts.stableSeries")
                .contains("lossTypeSeries = stableChartSeries")
                .contains("productionSeries = stableChartSeries")
                .contains("lostCansSeries = stableChartSeries")
                .contains("unexplainedSeries = stableChartSeries")
                .contains("categoricalChartOptions")
                .contains("options:sensorFive ? categoricalChartOptions : chartOptions")
                .doesNotContain("singleLossType");
        assertThat(charts)
                .contains("minimumSlots = 4")
                .contains("autoSkip: !categorical")
                .contains("positiveValueLabels");
    }

    @Test
    void keepsSharedUiSettingsOutsideFeatureTemplates() throws Exception {
        String report = Files.readString(Path.of("src/main/resources/templates/features/report/report.html"));
        String planFact = Files.readString(Path.of("src/main/resources/templates/features/shift/shift.html"));
        String comments = Files.readString(Path.of("src/main/resources/templates/features/comment/comment.html"));
        String charts = Files.readString(Path.of("src/main/resources/static/js/operational-charts.js"));

        assertThat(report).contains("/js/operational-charts.js");
        assertThat(planFact).contains("/js/operational-charts.js");
        assertThat(planFact).doesNotContain("<style>");
        assertThat(comments).doesNotContain("<style>");
        assertThat(charts).contains("autoSkip: !categorical", "minimumSlots = 4");
    }
}
