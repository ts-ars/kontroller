package com.exempal.shiftcounter.features.settings.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsStorageTest {

    private SettingsStorage storage;

    @BeforeEach
    void setUp() {
        storage = new SettingsStorage();
    }

    @Test
    void shouldReturnDefaultValues() {
        assertEquals(180, storage.getPpm());
        assertEquals(
                List.of("08:00", "09:00", "10:00", "11:00", "12:30", "13:30", "14:30", "15:30"),
                storage.getHours()
        );
        assertEquals(
                List.of(200, 200, 200, 200, 200, 200, 200, 200),
                storage.getHourlyPlans()
        );
    }

    @Test
    void shouldUpdatePpmCorrectly() {
        storage.setPpm(200);
        assertEquals(200, storage.getPpm());
    }

    @Test
    void shouldUpdateHoursAndPlans() {
        List<String> newHours = List.of("06:00", "07:00");
        List<Integer> newPlans = List.of(100, 110);

        storage.setHours(newHours);
        storage.setHourlyPlans(newPlans);

        assertEquals(newHours, storage.getHours());
        assertEquals(newPlans, storage.getHourlyPlans());
    }

    @Test
    void updateIsByReferenceSafe() {
        List<String> hoursBefore = storage.getHours();

        assertThrows(UnsupportedOperationException.class, () -> {
            hoursBefore.set(0, "99:99");
        }, "getHours() должен возвращать неизменяемый список");
    }
}
