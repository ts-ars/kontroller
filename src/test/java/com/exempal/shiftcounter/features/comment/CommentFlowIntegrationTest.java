package com.exempal.shiftcounter.features.comment;


import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ApplicationEventPublisher events;
    @Autowired
    StoppageRepository stoppages;
    @Autowired JpaShiftAdapter shiftAdapter;

    @BeforeEach
    void clearStoppages() {
        stoppages.deleteAll();
    }

    @Test
    void fullCommentFlow() throws Exception {
        TestUtils.stopAt(events, "08:20", 10);

        LocalDate today = LocalDate.now();
        ShiftEntity shift = shiftAdapter.findEntityByDate(today).orElseThrow();
        List<StoppageEntry> all = stoppages.findByShiftDate(today);
        assertThat(all).hasSize(1);

        String json = """
    [
      {
        "time": "08:20",
        "minutes": 10,
        "type": "material",
        "comment": "Roll change"
      }
    ]
    """;

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/comments/range")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                // 💥 находим запись с нужными полями, а не берем $[0]
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[?(@.type == 'material' && @.comment == 'Roll change')]"
                ).exists());
    }
}
