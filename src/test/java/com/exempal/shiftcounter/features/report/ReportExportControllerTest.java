package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.report.application.ReportView;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportExportControllerTest {
    @Test
    void exportsSelectedSensorAndRangeWithMeaningfulFilename() {
        ReportQueryUseCase reports = mock(ReportQueryUseCase.class);
        Map<String, String> params = Map.of("sensorId", "sensor-5", "from", "2026-08-01", "to", "2026-08-08");
        when(reports.query(params)).thenReturn(new ReportView(List.of(), LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 8), "sensor-5", 0, 0, List.of(), List.of(), "weekly"));
        var response = new ReportExportController(reports, new ReportExcelExporter()).export(params);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("shift-report_sensor-5_2026-08-01_to_2026-08-08.xlsx");
        assertThat(response.getBody()).startsWith(new byte[]{'P', 'K'});
        verify(reports).query(params);
    }
}
