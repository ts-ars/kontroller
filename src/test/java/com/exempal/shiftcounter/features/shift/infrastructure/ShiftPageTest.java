package com.exempal.shiftcounter.features.shift.adapter.persistence;

import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftSlice;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ShiftPageTest {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 7);
    private ShiftProjectionUseCase projection;
    private ShiftPage page;

    @BeforeEach
    void setUp() {
        projection = mock(ShiftProjectionUseCase.class);
        page = new ShiftPage(projection, new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-07T08:00:00Z"), ZoneOffset.UTC)));
        when(projection.buildView(eq(DATE), anyString(), any())).thenAnswer(invocation ->
                view(invocation.getArgument(1)));
    }

    @Test
    void defaultsToFourIndependentDayViews() {
        ConcurrentModel model = new ConcurrentModel();

        page.populateModel(model);

        assertThat((List<ShiftView>) model.getAttribute("views"))
                .extracting(ShiftView::sensorId)
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4");
        assertThat(model.getAttribute("shiftSlice")).isEqualTo("day");
        verify(projection, times(6)).buildView(eq(DATE), anyString(), eq(ShiftSlice.DAY));
    }

    @Test
    void selectsSensorFiveAndSixEveningViews() {
        ConcurrentModel model = new ConcurrentModel();

        page.populateModel(model, Map.of("sensors", "5-6", "shift", "evening"));

        assertThat((List<ShiftView>) model.getAttribute("views"))
                .extracting(ShiftView::sensorId).containsExactly("sensor-5", "sensor-6");
        assertThat(model.getAttribute("shiftSlice")).isEqualTo("evening");
        verify(projection, times(6)).buildView(eq(DATE), anyString(), eq(ShiftSlice.EVENING));
    }

    @Test void pageNameShouldBeShift() { assertThat(page.getPageName()).isEqualTo("shift"); }

    @Test
    void templateKeepsReferenceLayoutAndRemovesBrokenCommentInputPath() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/features/shift/shift.html"));
        assertThat(template).contains("class=\"sensor-grid\"", "th:each=\"view : ${views}\"",
                "backgroundColor: '#3b82f6'", "/topic/shift-updates/${view.sensorId}",
                "/topic/comments/${view.sensorId}", "15:00–23:00");
        assertThat(template).doesNotContain("input[name=\"comment\"]", "/topic/shift-comments");
    }

    private ShiftView view(String sensorId) {
        return new ShiftView(DATE, sensorId, List.of(1), List.of(2), List.of("07:00"), List.of(true));
    }
}
