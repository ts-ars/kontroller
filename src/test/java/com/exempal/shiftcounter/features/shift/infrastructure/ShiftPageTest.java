package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.comment.StoppageRepository;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftPageTest {

    private ShiftPlannerUseCase planner;
    private ShiftPage page;

    @BeforeEach
    void setUp() {
        planner = mock(ShiftPlannerUseCase.class);
        StoppageRepository stoppages = mock(StoppageRepository.class); // ⬅️ добавлено
        page = new ShiftPage(planner, stoppages);   // ⬅️ исправлено
    }

    @Test
    void shouldPopulateModelWithShiftData() {
        LocalDate today = LocalDate.now();
        Shift mockShift = ShiftTestFactory.with(today, 800, 700, "Недовыполнение");
        when(planner.buildShift(any())).thenReturn(mockShift);

        Model model = new ConcurrentModel();
        page.populateModel(model);

        assertEquals(800, model.getAttribute("plan"));
        assertEquals(700, model.getAttribute("actual"));
        assertEquals("Недовыполнение", model.getAttribute("comment"));
    }

    @Test
    void pageNameShouldBeShift() {
        assertEquals("shift", page.getPageName());
    }
}
