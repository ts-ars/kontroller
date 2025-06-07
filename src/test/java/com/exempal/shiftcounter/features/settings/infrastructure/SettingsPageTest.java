package com.exempal.shiftcounter.features.settings.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingsPageTest {

    private SettingsPage page;
    private SettingsStorage storage;

    @BeforeEach
    void setUp() {
        storage = mock(SettingsStorage.class);

        when(storage.getHours()).thenReturn(List.of("08:00", "09:00", "10:00"));
        when(storage.getHourlyPlans()).thenReturn(List.of(100, 110, 120));
        when(storage.getPpm()).thenReturn(200);

        page = new SettingsPage(storage);
    }

    @Test
    void getPageName_shouldReturnSettings() {
        assertEquals("settings", page.getPageName());
    }

    @Test
    void populateModel_shouldAddHoursPlansAndPpm() {
        Model model = new ConcurrentModel();

        page.populateModel(model);

        assertEquals(List.of("08:00", "09:00", "10:00"), model.getAttribute("hours"));
        assertEquals(List.of(100, 110, 120), model.getAttribute("plans"));
        assertEquals(200, model.getAttribute("ppm"));
    }

    @Test
    void updateSettings_shouldSaveAndRedirect() {
        List<String> hours = List.of("07:00", "08:00");
        List<Integer> plans = List.of(90, 100);

        String result = page.updateSettings(180, hours, plans);

        verify(storage).setPpm(180);
        verify(storage).setHours(hours);
        verify(storage).setHourlyPlans(plans);

        assertEquals("redirect:/settings", result);
    }
}
