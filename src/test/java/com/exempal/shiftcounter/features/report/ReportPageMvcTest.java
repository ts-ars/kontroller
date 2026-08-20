package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.core.PageController;
import com.exempal.shiftcounter.core.PageModelResolver;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.report.application.ReportRow;
import com.exempal.shiftcounter.features.report.application.ReportLossTotal;
import com.exempal.shiftcounter.features.report.application.ReportView;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.application.CommentActor;
import com.exempal.shiftcounter.features.comment.application.CurrentCommentActor;
import com.exempal.shiftcounter.features.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc
@Import({PageModelResolver.class, ReportPage.class})
class ReportPageMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportQueryUseCase reports;

    @MockBean
    private CurrentCommentActor actors;

    @BeforeEach
    void currentActor() {
        when(actors.require()).thenReturn(new CommentActor(UUID.randomUUID(), "Operator", UserRole.USER));
    }

    @Test
    @WithMockUser(username = "Operator", roles = "USER")
    void mapsRangeSensorFiveRowsSourcesAndChartTotalsToTheMvcModel() throws Exception {
        when(reports.query(Map.of("from", "2026-08-09", "to", "2026-08-10", "sensorId", "sensor-5")))
                .thenReturn(new ReportView(
                        List.of(new ReportRow("sensor-2", LossCategory.BREAKDOWN, 12, 48, "belt")),
                        LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10), "sensor-5", 12, 48,
                        List.of(
                                new ReportLossTotal("sensor-1", 10),
                                new ReportLossTotal("sensor-2", 20),
                                new ReportLossTotal("sensor-3", 30),
                                new ReportLossTotal("sensor-4", 40))));
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
                .andExpect(model().attribute("lossTotals", hasSize(4)))
                .andExpect(model().attribute("sensorOptions", hasSize(6)))
                .andExpect(content().string(not(containsString("<h1>Shift Report</h1>"))))
                .andExpect(content().string(containsString(">Filter</button>")))
                .andExpect(content().string(allOf(
                        containsString("aria-label=\"Sensor\""),
                        containsString(">1</a>"), containsString(">2</a>"),
                        containsString(">3</a>"), containsString(">4</a>"),
                        containsString(">5</a>"), containsString(">6</a>"))))
                .andExpect(content().string(allOf(
                        containsString("from=2026-08-09"),
                        containsString("to=2026-08-10"),
                        containsString("sensorId=sensor-6"))))
                .andExpect(content().string(allOf(
                        containsString("name=\"sensorId\" value=\"sensor-5\""),
                        containsString("name=\"from\" value=\"2026-08-09\""),
                        containsString("name=\"to\" value=\"2026-08-10\""),
                        containsString("class=\"report-table "))))
                .andExpect(content().string(containsString("report-charts")))
                .andExpect(content().string(containsString(">Source<span class=\"report-filter-icon\"")))
                .andExpect(content().string(containsString(">Author<span class=\"report-filter-icon\"")))
                .andExpect(content().string(containsString("Export Excel")))
                .andExpect(content().string(containsString("sensor-2")))
                .andExpect(content().string(containsString("id=\"lossChart\"")))
                .andExpect(content().string(containsString("id=\"cansChart\"")))
                .andExpect(content().string(containsString("sensorFive ? 'lossSensors' : 'lossTime'")));
    }

    @Test
    @WithMockUser(username = "Operator", roles = "USER")
    void ordinarySensorRendersBothRequiredCharts() throws Exception {
        when(reports.query(Map.of("sensorId", "sensor-6"))).thenReturn(new ReportView(
                List.of(), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 10),
                "sensor-6", 0, 0, List.of(new ReportLossTotal("sensor-6", 3))));

        mvc.perform(get("/page/report").param("sensorId", "sensor-6"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"active\">6</a>")))
                .andExpect(content().string(containsString("id=\"lossChart\"")))
                .andExpect(content().string(containsString("id=\"cansChart\"")))
                .andExpect(content().string(containsString("id=\"productionChart\"")))
                .andExpect(content().string(containsString("id=\"unexplainedChart\"")))
                .andExpect(content().string(containsString("sensorFive ? 'lossSensors' : 'lossTime'")));
    }
}
