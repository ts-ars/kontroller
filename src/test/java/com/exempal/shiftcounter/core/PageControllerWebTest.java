package com.exempal.shiftcounter.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("web")
class PageControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Оставляем только нужные моки, которые нельзя подставить настоящими
    @MockBean
    private StoppageRepository stoppageRepository;

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
        mockMvc.perform(get("/page/report"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "report"));
    }
}
