package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.comment.application.LossExplanationRepository;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.StoppageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


class ReportPageTest {

    private ReportPage page;
    private StoppageRepository repo;
    private LossExplanationRepository explanations;

    @BeforeEach
    void setUp() {
        repo = mock(StoppageRepository.class);
        explanations = mock(LossExplanationRepository.class);
        StoppageEntry entry = mock(StoppageEntry.class);
        when(entry.getId()).thenReturn(1L);
        when(entry.getType()).thenReturn(StoppageType.FIXED);

        when(repo.findByShiftDateBetween(any(), any())).thenReturn(List.of(entry));
        when(explanations.findByStoppageId(anyLong())).thenReturn(List.of(
                new LossExplanation(1L, 1L, LossCategory.BREAKDOWN, "belt", 10, 400)));
        page = new ReportPage(repo, explanations);
    }

    @Test
    void getPageName_shouldReturnReport() {
        assertEquals("report", page.getPageName());
    }

    @Test
    void populateModel_shouldAddProblemsAndTotals() {
        Model model = new ConcurrentModel();

        page.populateModel(model, Map.of());

        assertNotNull(model.getAttribute("problems"));
        assertInstanceOf(List.class, model.getAttribute("problems"));

        assertEquals(10, model.getAttribute("totalMinutes"));
        assertEquals(400, model.getAttribute("totalCans"));

        assertNotNull(model.getAttribute("startDate"));
        assertNotNull(model.getAttribute("endDate"));
    }
}
