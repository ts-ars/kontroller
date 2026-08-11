package com.exempal.shiftcounter.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.report.application.ReportSignalQueryPort;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Оставляем только нужные моки, которые нельзя подставить настоящими
    @MockBean
    private StoppageRepository stoppageRepository;

    @MockBean
    private ReportSignalQueryPort reportSignals;

    @Test
    void shiftPageShouldReturnOkAndLayout() throws Exception {
        mockMvc.perform(get("/page/shift"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "shift"));
    }

    @Test
    void settingsPageShouldReturnOkAndLayout() throws Exception {
        mockMvc.perform(get("/page/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "settings"));
    }

    @Test
    void reportPageShouldReturnOkAndLayout() throws Exception {
        mockMvc.perform(get("/page/report")
                        .param("from", "2026-08-09")
                        .param("to", "2026-08-10")
                        .param("sensorId", "sensor-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "report"))
                .andExpect(model().attribute("startDate", "2026-08-09"))
                .andExpect(model().attribute("endDate", "2026-08-10"))
                .andExpect(model().attribute("sensorId", "sensor-5"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"sensorId\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sensor-6")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("report-charts sensor-five")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Source")));
    }
}
