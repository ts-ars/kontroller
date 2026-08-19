package com.exempal.shiftcounter.features.report.adapter;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportPageTest {
    private ReportPage page;

    @BeforeEach
    void setUp() {
        StoppageRepository repository = mock(StoppageRepository.class);
        LossExplanation explanation = new LossExplanation(1L, 1L, LossCategory.BREAKDOWN, "belt", 10, 400);
        Stoppage stoppage = new Stoppage(1L, UUID.randomUUID(), 1L, Stoppage.PRIMARY_SENSOR, 0,
                LocalDateTime.of(2026, 8, 7, 8, 0), Duration.ofMinutes(10), 10, 400,
                DetectionType.FIXED, StoppageState.ACTIVE, List.of(explanation), 0L);
        when(repository.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-1")))
                .thenReturn(List.of(stoppage));
        page = new ReportPage(new ReportQueryUseCase(repository, new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-07T08:00:00Z"), ZoneOffset.UTC)),
                mock(ActualDataPort.class), new ShiftIntervalService()));
    }

    @Test
    void getPageName_shouldReturnReport() {
        assertEquals("report", page.getPageName());
    }

    @Test
    void populateModel_shouldAddProblemsAndTotals() {
        Model model = new ConcurrentModel();
        page.populateModel(model, Map.of());
        assertInstanceOf(List.class, model.getAttribute("problems"));
        assertEquals(10, model.getAttribute("totalMinutes"));
        assertEquals(400, model.getAttribute("totalCans"));
        assertNotNull(model.getAttribute("startDate"));
        assertNotNull(model.getAttribute("endDate"));
        assertInstanceOf(List.class, model.getAttribute("lossTotals"));
        assertEquals(6, ((List<?>) model.getAttribute("sensorOptions")).size());
    }
}
