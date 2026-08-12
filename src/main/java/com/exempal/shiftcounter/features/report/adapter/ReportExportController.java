package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportExportController {
    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final ReportQueryUseCase reports;
    private final ReportExcelExporter exporter;

    public ReportExportController(ReportQueryUseCase reports, ReportExcelExporter exporter) {
        this.reports = reports;
        this.exporter = exporter;
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> export(@RequestParam Map<String, String> params) {
        var report = reports.query(params);
        String filename = "shift-report_" + report.sensorId() + "_" + report.from() + "_to_" + report.to() + ".xlsx";
        return ResponseEntity.ok().contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(exporter.export(report));
    }
}
