package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.core.PageController;
import com.exempal.shiftcounter.core.PageModelResolver;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.report.application.ReportRow;
import com.exempal.shiftcounter.features.report.application.ReportSignalTotal;
import com.exempal.shiftcounter.features.report.application.ReportView;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportPageMvcTest {
    @Test
    void mapsRangeSensorFiveRowsSourcesAndChartTotalsToTheMvcModel() throws Exception {
        ReportQueryUseCase reports = mock(ReportQueryUseCase.class);
        when(reports.query(Map.of("from", "2026-08-09", "to", "2026-08-10", "sensorId", "sensor-5")))
                .thenReturn(new ReportView(
                        List.of(new ReportRow("sensor-2", LossCategory.BREAKDOWN, 12, 48, "belt")),
                        LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), "sensor-5", 12, 48,
                        List.of(
                                new ReportSignalTotal("sensor-1", 10),
                                new ReportSignalTotal("sensor-2", 20),
                                new ReportSignalTotal("sensor-3", 30),
                                new ReportSignalTotal("sensor-4", 40))));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PageController(
                new PageModelResolver(List.of(new ReportPage(reports))))).build();

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
                .andExpect(model().attribute("sensorOptions", hasSize(6)));
    }
}
