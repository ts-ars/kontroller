package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.Tag("unit")
public class ShiftPageTest {

    private ShiftProjectionUseCase projection;
    private ShiftPage page;

    @BeforeEach
    void setUp() {
        projection = mock(ShiftProjectionUseCase.class);
        page = new ShiftPage(projection);
    }

    @Test
    void shouldPopulateModelWithShiftData() {
        LocalDate today = LocalDate.now();

        List<Integer> actual = List.of(100, 200, 300);
        List<Integer> plan = List.of(150, 250, 400);
        List<String> hours = List.of("08:00", "09:00", "10:00");

        ShiftView view = new ShiftView(today, actual, plan, hours);

        when(projection.buildView(any())).thenReturn(view);

        Model model = new ConcurrentModel();
        page.populateModel(model);

        assertEquals(plan, model.getAttribute("plan"));
        assertEquals(actual, model.getAttribute("actual"));
        assertEquals(today, model.getAttribute("date"));
        assertEquals(hours, model.getAttribute("hours"));
    }

    @Test
    void pageNameShouldBeShift() {
        assertEquals("shift", page.getPageName());
    }
}
