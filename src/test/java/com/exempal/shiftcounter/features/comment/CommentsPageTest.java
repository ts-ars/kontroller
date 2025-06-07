package com.exempal.shiftcounter.features.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentsPageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoppageRepository stoppageRepository;

    @Test
    void commentsPageShouldReturnOkAndPassStoppagesToModel() throws Exception {
        List<StoppageEntry> stoppages = List.of(
                createEntry("10:00", 15.0, 300, "organization", "нет оператора", LocalDate.of(2024, 6, 1)),
                createEntry("11:00", 10.0, 200, "material", "", LocalDate.of(2024, 6, 1))
        );

        when(stoppageRepository.findByDate(LocalDate.now())).thenReturn(stoppages);

        mockMvc.perform(get("/page/comment"))  // ← исправленный путь
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("currentPage", "comment"))
                .andExpect(model().attribute("rows", stoppages))
                .andExpect(model().attributeExists("alerts"));
    }

    private StoppageEntry createEntry(String time, double minutes, int cans, String type, String comment, LocalDate date) {
        StoppageEntry entry = new StoppageEntry();
        entry.setTime(time);
        entry.setMinutes(minutes);
        entry.setCans(cans);
        entry.setType(type);
        entry.setComment(comment);
        entry.setDate(date);
        return entry;
    }
}
