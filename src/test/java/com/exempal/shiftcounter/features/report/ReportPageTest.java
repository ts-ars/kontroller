package com.exempal.shiftcounter.features.report;

import com.exempal.shiftcounter.features.comment.StoppageEntry;
import com.exempal.shiftcounter.features.comment.StoppageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportPageTest {

    private ReportPage page;
    private StoppageRepository repo;

    @BeforeEach
    void setUp() {
        repo = mock(StoppageRepository.class);

        // ✳️ Создаём фиктивную остановку вручную через сеттеры
        StoppageEntry entry = new StoppageEntry();
        entry.setTime("08:00");
        entry.setType("breakdown");
        entry.setComment("belt");
        entry.setMinutes(10);
        entry.setCans(400);
        entry.setDate(LocalDate.now());

        when(repo.findByDateBetween(any(), any())).thenReturn(List.of(entry));

        page = new ReportPage(repo);
    }

    @Test
    void getPageName_shouldReturnReport() {
        assertEquals("report", page.getPageName());
    }

    @Test
    void populateModel_shouldAddProblemsAndTotals() {
        Model model = new ConcurrentModel();

        // вызываем двухаргументный метод
        page.populateModel(model, Map.of());

        assertNotNull(model.getAttribute("problems"));
        assertTrue(model.getAttribute("problems") instanceof List<?>);

        assertEquals(10, model.getAttribute("totalMinutes"));
        assertEquals(400, model.getAttribute("totalCans"));

        assertNotNull(model.getAttribute("startDate"));
        assertNotNull(model.getAttribute("endDate"));
    }
}
