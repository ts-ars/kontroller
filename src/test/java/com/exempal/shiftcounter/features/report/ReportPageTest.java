package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageRepository;
import com.exempal.shiftcounter.features.comment.StoppageTestFactory;
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

    @BeforeEach
    void setUp() {
        repo = mock(StoppageRepository.class);
        StoppageEntry entry = StoppageTestFactory.defaultBreakdownToday();

        when(repo.findByShiftDateBetween(any(), any())).thenReturn(List.of(entry));
        page = new ReportPage(repo);
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
