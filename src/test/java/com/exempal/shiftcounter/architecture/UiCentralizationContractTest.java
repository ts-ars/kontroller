package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class UiCentralizationContractTest {
    private static final Path STYLES = Path.of("src/main/resources/static/css/styles.css");
    private static final Path CHARTS = Path.of("src/main/resources/static/js/operational-charts.js");
    private static final List<Path> FEATURES = List.of(
            Path.of("src/main/resources/templates/features/shift/shift.html"),
            Path.of("src/main/resources/templates/features/comment/comment.html"),
            Path.of("src/main/resources/templates/features/report/report.html"),
            Path.of("src/main/resources/templates/features/settings/settings.html"),
            Path.of("src/main/resources/templates/features/users/users.html"));

    @Test
    void everyFeatureUsesTheSharedSemanticUiContract() throws Exception {
        for (Path feature : FEATURES) {
            String html = read(feature);
            assertThat(html).as(feature.toString())
                    .contains("feature-page", "ui-page")
                    .doesNotContain("<style", "style=", "new Chart(", "OperationalCharts.options");
        }

        assertThat(read(FEATURES.get(0))).contains("ui-adaptive-grid", "ui-chart-frame", "ui-table", "ui-table--cards");
        assertThat(read(FEATURES.get(1))).contains("ui-filter-bar", "ui-adaptive-grid", "ui-section", "ui-table", "ui-table--cards", "ui-actions");
        assertThat(read(FEATURES.get(2))).contains("ui-filter-bar", "ui-chart-grid", "ui-chart-frame", "ui-table", "ui-table--cards", "ui-actions");
        assertThat(read(FEATURES.get(3))).contains("ui-section", "ui-table", "ui-table--cards", "ui-actions");
        assertThat(read(FEATURES.get(4))).contains("ui-section", "ui-card", "ui-actions");
    }

    @Test
    void stylesExposeOneTokenizedResponsiveContractWithoutOverflowEscapes() throws Exception {
        String css = read(STYLES);
        assertThat(css).contains(
                "/* 1. Tokens */", "/* 2. Base */", "/* 3. Shell */", "/* 4. Controls */",
                "/* 5. Grids and sections */", "/* 6. Charts */", "/* 7. Tables */",
                "/* 8. Mobile table cards */", "/* 9. Responsive contract */", "/* 10. Feature exceptions */",
                "--ui-color-primary", "--ui-border", "--ui-radius-control", "--ui-control-height",
                "--ui-page-max", "--ui-chart-height", "--ui-space-page");
        assertThat(css).doesNotContain("overflow-x", "100vw");
        var minWidths = Pattern.compile("min-width\\s*:\\s*([^;}\\r\\n]+)", Pattern.CASE_INSENSITIVE)
                .matcher(css).results().map(result -> result.group(1).trim()).toList();
        assertThat(minWidths).allMatch("0"::equals);
    }

    @Test
    void chartLifecycleAndPresentationStayCentralized() throws Exception {
        String charts = read(CHARTS);
        assertThat(charts).contains("const colors", "const profiles = Object.freeze", "create", "update", "destroy");
        for (Path feature : List.of(FEATURES.get(0), FEATURES.get(2))) {
            assertThat(read(feature)).contains("OperationalCharts.create")
                    .doesNotContain("new Chart(", "responsive:", "maintainAspectRatio:", "plugins:", "scales:");
        }
    }

    @Test
    void knownLegacyComponentRulesAreNotDuplicatedOrLocallyReimplemented() throws Exception {
        String css = read(STYLES);
        assertThat(occurrences(css, ".report-chart-card {")).isEqualTo(1);
        assertThat(occurrences(css, "\n#comments-page .actions {")).isEqualTo(1);
        assertThat(occurrences(css, "\n#comments-page .actions button {")).isEqualTo(1);
        assertThat(css).doesNotContain(
                "#settings-page { font-family:",
                "#settings-page button {",
                "#settings-page button:hover",
                "height: 34px; padding: 4px 6px; font-size: 14px");
    }

    private static int occurrences(String value, String fragment) {
        return (value.length() - value.replace(fragment, "").length()) / fragment.length();
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path);
    }
}
