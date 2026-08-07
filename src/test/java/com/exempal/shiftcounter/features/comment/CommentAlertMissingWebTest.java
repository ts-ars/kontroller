package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.adapter.CommentsPage;
import com.exempal.shiftcounter.features.shift.application.ShiftInitializerService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Tag("web")
class CommentAlertMissingWebTest {

    @Autowired ApplicationEventPublisher events;
    @Autowired
    CommentsPage commentsPage;
    @Autowired MockMvc mockMvc;
    @Autowired ShiftInitializerService shiftInitializerService;

    @Test
    @org.junit.jupiter.api.Disabled("Stage 2: legacy comment alerts aggregate by interval instead of matching approved explanations")
    void alertsShownForMissingComments() throws Exception {
        LocalDate today = LocalDate.now();
        shiftInitializerService.createNewShift(today);
        TestUtils.stopAt(events, "08:20", 10);
        TestUtils.stopAt(events, "09:10", 5);

        String json = """
        [
          {
            "time": "08:20",
            "minutes": 10,
            "type": "organization",
            "comment": "Scheduled pause"
          }
        ]
        """;

        mockMvc.perform(post("/api/comments")
                        .param("date", today.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        var model = TestUtils.newModel();
        commentsPage.populateModel(model);
        List<?> alerts = (List<?>) model.getAttribute("alerts");

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).toString()).contains("09:10");
    }
}
