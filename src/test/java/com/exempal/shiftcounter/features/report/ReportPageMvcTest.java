package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.core.PageController;
import com.exempal.shiftcounter.core.PageModelResolver;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.report.application.ReportRow;
import com.exempal.shiftcounter.features.report.application.ReportSignalTotal;
import com.exempal.shiftcounter.features.report.application.ReportView;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PageModelResolver.class, ReportPage.class})
class ReportPageMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportQueryUseCase reports;

    @Test
    void mapsRangeSensorFiveRowsSourcesAndChartTotalsToTheMvcModel() throws Exception {
        when(reports.query(Map.of("from", "2026-08-09", "to", "2026-08-10", "sensorId", "sensor-5")))
                .thenReturn(new ReportView(
                        List.of(new ReportRow("sensor-2", LossCategory.BREAKDOWN, 12, 48, "belt")),
                        LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), "sensor-5", 12, 48,
                        List.of(
                                new ReportSignalTotal("sensor-1", 10),
                                new ReportSignalTotal("sensor-2", 20),
                                new ReportSignalTotal("sensor-3", 30),
                                new ReportSignalTotal("sensor-4", 40))));
        mvc.perform(get("/page/report")
                        .param("from", "2026-08-09")
                        .param("to", "2026-08-10")
                        .param("sensorId", "sensor-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "report"))
                .andExpect(model().attribute("startDate", "2026-08-09"))
                .andExpect(model().attribute("endDate", "2026-08-10"))
                .andExpect(model().attribute("sensorId", "sensor-5"))
                .andExpect(model().attribute("problems", hasSize(1)))
                .andExpect(model().attribute("signalTotals", hasSize(4)))
                .andExpect(model().attribute("sensorOptions", hasSize(6)))
                .andExpect(content().string(containsString("Shift Report")))
                .andExpect(content().string(containsString(">Filter</button>")))
                .andExpect(content().string(containsString("sensorId=sensor-6")))
                .andExpect(content().string(allOf(
                        containsString("report-charts"), containsString("sensor-five"))))
                .andExpect(content().string(containsString(">Source</th>")))
                .andExpect(content().string(containsString("sensor-2")))
                .andExpect(content().string(containsString("id=\"lossChart\"")))
                .andExpect(content().string(containsString("id=\"signalChart\"")));
    }

    @Test
    void ordinarySensorRendersOnlyLossChart() throws Exception {
        when(reports.query(Map.of("sensorId", "sensor-6"))).thenReturn(new ReportView(
                List.of(), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 10),
                "sensor-6", 0, 0, List.of(new ReportSignalTotal("sensor-6", 3))));

        mvc.perform(get("/page/report").param("sensorId", "sensor-6"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"lossChart\"")))
                .andExpect(content().string(not(containsString("id=\"signalChart\""))));
    }
}
