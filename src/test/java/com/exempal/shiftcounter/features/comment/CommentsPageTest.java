package com.exempal.shiftcounter.features.comment.adapter.web;

import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageTimeService;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.core.PageController;
import com.exempal.shiftcounter.core.PageModelResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentsPageTest {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 7);

    @Test
    void defaultsToSensorFiveAndCurrentShift() {
        CommentsReadUseCase useCase = mock(CommentsReadUseCase.class);
        when(useCase.read(DATE, "sensor-5"))
                .thenReturn(new CommentsReadUseCase.Data(null, List.of(), List.of(), List.of()));
        ConcurrentModel model = new ConcurrentModel();

        page(useCase).populateModel(model);

        assertThat(model.getAttribute("sensorId")).isEqualTo("sensor-5");
        assertThat(model.getAttribute("shiftSlice")).isEqualTo("day");
        assertThat(model.getAttribute("readOnlyAggregation")).isEqualTo(true);
        verify(useCase).read(DATE, "sensor-5");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-5", "sensor-6"})
    void acceptsEveryCatalogSensor(String sensorId) {
        CommentsReadUseCase useCase = mock(CommentsReadUseCase.class);
        when(useCase.read(DATE, sensorId)).thenReturn(new CommentsReadUseCase.Data(null, List.of(), List.of()));
        CommentsPage page = page(useCase);
        ConcurrentModel model = new ConcurrentModel();

        page.populateModel(model, Map.of("sensorId", sensorId));

        assertThat(model.getAttribute("sensorId")).isEqualTo(sensorId);
        verify(useCase).read(DATE, sensorId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-5", "sensor-6"})
    void mvcRoutesEveryCatalogSensorAndEveningSlice(String sensorId) throws Exception {
        CommentsReadUseCase useCase = mock(CommentsReadUseCase.class);
        when(useCase.read(DATE, sensorId)).thenReturn(new CommentsReadUseCase.Data(null, List.of(), List.of()));
        var resolver = new PageModelResolver(List.of(page(useCase)));
        var mvc = MockMvcBuilders.standaloneSetup(new PageController(resolver)).build();

        mvc.perform(get("/page/comment").param("sensorId", sensorId).param("shift", "evening"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("sensorId", sensorId))
                .andExpect(model().attribute("shiftSlice", "evening"));
    }

    @Test
    void sensorFiveEveningIncludesRowsAfterTwentyThreeAndExcludesDayRows() {
        CommentsReadUseCase useCase = mock(CommentsReadUseCase.class);
        var day = new CommentsReadUseCase.ExplanationRow("sensor-1", DATE.atTime(9, 0),
                LossCategory.MATERIAL, "Day", 2);
        var afterTwentyThree = new CommentsReadUseCase.ExplanationRow("sensor-1",
                DATE.atTime(23, 30), LossCategory.QUALITY, "Late", 3);
        var afterMidnight = new CommentsReadUseCase.ExplanationRow("sensor-1",
                DATE.plusDays(1).atTime(1, 30), LossCategory.QUALITY, "After midnight", 4);
        List<CommentsReadUseCase.SourceComments> sources = List.of(
                new CommentsReadUseCase.SourceComments("sensor-1", List.of(day, afterTwentyThree, afterMidnight)),
                new CommentsReadUseCase.SourceComments("sensor-2", List.of()),
                new CommentsReadUseCase.SourceComments("sensor-3", List.of()),
                new CommentsReadUseCase.SourceComments("sensor-4", List.of()));
        when(useCase.read(DATE, "sensor-5"))
                .thenReturn(new CommentsReadUseCase.Data(null, List.of(), List.of(), sources));
        ConcurrentModel model = new ConcurrentModel();

        page(useCase).populateModel(model, Map.of("sensorId", "sensor-5", "shift", "evening"));

        var filtered = (List<CommentsReadUseCase.SourceComments>) model.getAttribute("sourceComments");
        assertThat(filtered.getFirst().rows()).extracting(CommentsReadUseCase.ExplanationRow::comment)
                .containsExactly("Late", "After midnight");
        assertThat(model.getAttribute("readOnlyAggregation")).isEqualTo(true);
    }

    @Test
    void templateContainsEditableReadonlyAndSensorFiveReferenceLayouts() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/features/comment/comment.html"));
        String styles = Files.readString(Path.of("src/main/resources/static/css/styles.css"));
        assertThat(template).contains("class=\"inherited-grid adaptive-two-column-grid ui-adaptive-grid\"", "readonly aria-label=\"Allocated cans\"",
                "Category", "Comment", "Minutes", "Allocated cans", "sensor-5", "15:00–23:00");
        assertThat(template).doesNotContain("Reasons for Stoppages", "name=\"comment\"");
        assertThat(template).contains("function editorState()", "function restoreEditorState(state)",
                "client.subscribe(`/topic/comments/${selectedCommentsSensor}`, refreshComments)",
                "client.subscribe(`/topic/shift-updates/${selectedCommentsSensor}`, refreshComments)",
                "document.querySelector('.explanations tr:not([data-explanation-id])')",
                "adaptive-two-column-grid", "ui-table--cards",
                "mobile-column-label", ">Min<", ">Cans<", ">By<",
                "th:text=\"${source.totalMinutes}\"",
                "<section id=\"manual-fallback\" class=\"ui-section\">",
                "loss.detectionType == 'MANUAL'",
                "'openManualInterval()' : 'addExplanation(this)'");
        assertThat(template).doesNotContain("window.location.reload()",
                "width: 1100px; min-width: 1100px",
                "grid-template-columns: 115px minmax(0, 1fr)");
        assertThat(styles).contains("container-name: comment-card", "container-type: inline-size",
                "@container comment-card (max-width: 820px)",
                "@container (max-width: 1100px)",
                "#comments-page .inherited-table thead { display: none; }",
                "grid-template-areas: 'category minutes cans author actions' 'comment comment comment comment comment'",
                "grid-area: comment", "grid-area: actions");
    }

    private CommentsPage page(CommentsReadUseCase useCase) {
        var settings = mock(com.exempal.shiftcounter.features.shift.application.ShiftSettingsPort.class);
        when(settings.getForSensor(anyString())).thenReturn(
                new com.exempal.shiftcounter.features.shift.application.ShiftSettings(
                        List.of("07:00", "08:00"), List.of(60, 60)));
        return new CommentsPage(useCase, mock(StoppageTimeService.class), new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-07T08:00:00Z"), ZoneOffset.UTC)), settings,
                new com.exempal.shiftcounter.features.shift.application.ShiftIntervalService());
    }
}
