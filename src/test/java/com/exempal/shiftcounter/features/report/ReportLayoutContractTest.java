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
                .contains("--ui-page-max: 1740px")
                .contains("--ui-chart-height: 238px")
                .contains("--ui-chart-lift: 24px")
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
                .contains("--ui-chart-height-compact: 220px")
                .contains("--ui-chart-lift-compact: 20px");
        assertThat(css).doesNotContain("overflow-x", "min-width: 900px", "100vw");
        assertThat(report).contains("class=\"operational-chart-frame ui-chart-frame\"");
        assertThat(planFact).contains("class=\"chart-frame operational-chart-frame ui-chart-frame\"")
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
                .contains("OperationalCharts.create")
                .contains("'production'")
                .contains("'unexplained'")
                .contains("'lossTypes'")
                .contains("sensorFive ? 'lossSensors' : 'lossTime'")
                .doesNotContain("singleLossType", "categoricalChartOptions", "mobileLongLabels", "OperationalCharts.options");
        assertThat(charts)
                .contains("stableSlots: 4")
                .contains("profile.axis !== 'category'")
                .contains("positiveValueLabels")
                .contains("const compactDateLabel")
                .contains("'$3.$2'")
                .contains("'$2.$1'")
                .contains("lossTypes:")
                .contains("mobileRotation: 35")
                .contains("max-width: 600px");
    }

    @Test
    void keepsSharedUiSettingsOutsideFeatureTemplates() throws Exception {
        String report = Files.readString(Path.of("src/main/resources/templates/features/report/report.html"));
        String planFact = Files.readString(Path.of("src/main/resources/templates/features/shift/shift.html"));
        String comments = Files.readString(Path.of("src/main/resources/templates/features/comment/comment.html"));
        String settings = Files.readString(Path.of("src/main/resources/templates/features/settings/settings.html"));
        String charts = Files.readString(Path.of("src/main/resources/static/js/operational-charts.js"));

        assertThat(report).contains("/js/operational-charts.js", "OperationalCharts.create")
                .doesNotContain("OperationalCharts.options", "new Chart(");
        assertThat(planFact).contains("/js/operational-charts.js", "OperationalCharts.create")
                .doesNotContain("<style>", "OperationalCharts.options", "new Chart(");
        assertThat(comments).doesNotContain("<style>");
        assertThat(settings).doesNotContain("<style>");
        assertThat(charts)
                .contains("const profiles = Object.freeze", "stableSlots: 4", "profile.axis !== 'category'")
                .contains("production:", "unexplained:", "lossTypes:", "lossSensors:", "lossTime:", "planFactProduction:");
    }
}
