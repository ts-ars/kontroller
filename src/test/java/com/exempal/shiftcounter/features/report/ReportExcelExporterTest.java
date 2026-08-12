package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.report.application.ReportRow;
import com.exempal.shiftcounter.features.report.application.ReportView;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExcelExporterTest {
    @Test
    void createsRealSensorFiveXlsxWithSourceAuthorRowsAndTotal() throws Exception {
        ReportView report = new ReportView(List.of(
                row("sensor-2", 9, 40, "belt", "Alex"), row("sensor-1", 7, 40, "lid", "Maria")),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 8), "sensor-5", 16, 80,
                List.of(), List.of(), "weekly");
        byte[] workbook = new ReportExcelExporter().export(report);
        assertThat(workbook).startsWith(new byte[]{'P', 'K'});
        String sheet = zipEntry(workbook, "xl/worksheets/sheet1.xml");
        assertThat(sheet).contains("Source", "Type", "Minutes", "Cans", "Reason", "Author",
                "sensor-2", "Alex", "sensor-1", "Maria", "Total", "<v>16</v>", "<v>80</v>");
        assertThat(sheet.indexOf("sensor-2")).isLessThan(sheet.indexOf("sensor-1"));
    }

    @Test
    void ordinarySensorWorkbookOmitsSource() throws Exception {
        ReportView report = new ReportView(List.of(row("sensor-1", 2, 3, "reason", "Author")),
                LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 12), "sensor-1", 2, 3,
                List.of(), List.of(), "daily");
        String sheet = zipEntry(new ReportExcelExporter().export(report), "xl/worksheets/sheet1.xml");
        assertThat(sheet).doesNotContain(">Source<").contains("Author");
    }

    private ReportRow row(String source, int minutes, int cans, String reason, String author) {
        return new ReportRow(source, LossCategory.BREAKDOWN, minutes, cans, reason, author, LocalDate.of(2026, 8, 1));
    }

    private String zipEntry(byte[] workbook, String wanted) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(workbook))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (wanted.equals(entry.getName())) return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("Missing " + wanted);
    }
}
