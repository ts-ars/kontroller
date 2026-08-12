package com.exempal.shiftcounter.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Оставляем только нужные моки, которые нельзя подставить настоящими
    @MockBean
    private StoppageRepository stoppageRepository;

    @Test
    @WithMockUser(username = "Operator", roles = "USER")
    void shiftPageShouldReturnOkAndLayout() throws Exception {
        mockMvc.perform(get("/page/shift"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "shift"));
    }

    @Test
    @WithMockUser(username = "Administrator", roles = "ADMIN")
    void settingsPageShouldReturnOkAndLayout() throws Exception {
        mockMvc.perform(get("/page/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "settings"));
    }

    @Test
    @WithMockUser(username = "Operator", roles = "USER")
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
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("report-charts"),
                        org.hamcrest.Matchers.containsString("sensor-five"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Source")));
    }
}
