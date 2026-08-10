package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SettingsPageTest {

    private SettingsPort settings;
    private ShiftIntervalService intervals;
    private ShiftSettingsApplier settingsApplier;
    private SettingsPage page;

    @BeforeEach
    void setUp() {
        settings = mock(SettingsPort.class);
        intervals = mock(ShiftIntervalService.class);
        settingsApplier = mock(ShiftSettingsApplier.class);
        page = new SettingsPage(settings, intervals, settingsApplier);
    }

    @Test
    void testPopulateModel() {
        when(settings.getHourlyPlans()).thenReturn(List.of("100", "150", "200"));
        when(settings.getHours()).thenReturn(List.of("08:00", "09:00", "10:00"));

        Model model = new ExtendedModelMap();
        page.populateModel(model, Map.of());

        assertEquals(List.of(100, 150, 200), model.getAttribute("plans"));
        assertEquals(List.of("08:00", "09:00", "10:00"), model.getAttribute("hours"));
    }

    @Test
    void testUpdateSettings() {
        var redirect = page.updateSettings(
                List.of("10:00", "11:00", "12:00"),
                List.of(300, 350, 400)
        );

        assertEquals("redirect:/page/settings", redirect);

        verify(settings).updateHours(List.of("10:00", "11:00", "12:00"));
        verify(settings).updateHourlyPlans(List.of("300", "350", "400"));
        verify(intervals).resolve(java.time.LocalDate.of(2000, 1, 1),
                List.of("10:00", "11:00", "12:00"), 3);
        verify(settingsApplier).applySettingsToCurrentShift();
    }
}
