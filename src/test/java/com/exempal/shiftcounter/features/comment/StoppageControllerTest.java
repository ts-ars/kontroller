package com.exempal.shiftcounter.features.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoppageController.class)
class StoppageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoppageRepository stoppageRepository;

    @Test
    void shouldSaveStoppagesToDatabase() throws Exception {
        // given
        StoppageEntry entry = new StoppageEntry();
        entry.setTime("10:00");
        entry.setMinutes(15.0);
        entry.setCans(150);
        entry.setType("organization");
        entry.setComment("нет оператора");
        entry.setDate(LocalDate.now());

        List<StoppageEntry> entries = List.of(entry);

        // when
        mockMvc.perform(post("/api/comments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entries)))
                .andExpect(status().isOk());

        // then: проверка, что вызван saveAll с ожидаемыми значениями
        ArgumentCaptor<List<StoppageEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(stoppageRepository).saveAll(captor.capture());

        List<StoppageEntry> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        StoppageEntry savedEntry = saved.get(0);
        assertThat(savedEntry.getTime()).isEqualTo("10:00");
        assertThat(savedEntry.getMinutes()).isEqualTo(15.0);
        assertThat(savedEntry.getCans()).isEqualTo(150);
        assertThat(savedEntry.getType()).isEqualTo("organization");
        assertThat(savedEntry.getComment()).isEqualTo("нет оператора");
        assertThat(savedEntry.getDate()).isEqualTo(LocalDate.now());
    }
}
