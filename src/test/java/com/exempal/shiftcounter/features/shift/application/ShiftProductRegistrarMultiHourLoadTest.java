package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Tag("load")
class ShiftProductRegistrarMultiHourLoadTest {

    @Autowired
    private ShiftProductRegistrar registrar;

    @Autowired
    private ShiftPlannerUseCase planner;

    private LocalDate testDate;

    @BeforeEach
    void setup() {
        testDate = LocalDate.of(2025, 7, 9);
        Shift shift = planner.getOrCreateShift(testDate);
        System.out.println("🔁 Initial shift actual: " + shift.getHourlyActualValues());
    }

    private LocalDateTime midpointOfShiftHour(int index) {
        Shift shift = planner.getOrCreateShift(testDate);
        List<String> hours = shift.getHourlyLabels();

        LocalTime start = LocalTime.parse(hours.get(index));
        LocalTime end = index + 1 < hours.size()
                ? LocalTime.parse(hours.get(index + 1))
                : start.plusMinutes(start.getMinute() == 30 ? 30 : 60);
        long duration = java.time.Duration.between(start, end).toMinutes();

        return LocalDateTime.of(testDate, start.plusMinutes(duration / 2));
    }

    @Test
    void shouldDistribute999SignalsForFirst8HoursCorrectly() {
        int signalsPerHour = 999;

        Shift shift = planner.getOrCreateShift(testDate);
        List<String> hours = shift.getHourlyLabels();

        for (int i = 0; i < 8; i++) { // первые 8 интервалов
            LocalDateTime signalTime = midpointOfShiftHour(i);
            System.out.printf("➡️  [%s] %d сигналов в %s%n", hours.get(i), signalsPerHour, signalTime);

            for (int j = 0; j < signalsPerHour; j++) {
                registrar.registerProduct(signalTime);
            }

            int actual = planner.getOrCreateShift(testDate).getHourlyActualValues().get(i);
            if (actual != signalsPerHour) {
                fail(String.format("❌ [ПОСЛЕ] %s (index=%d): %d, ожидалось: %d",
                        hours.get(i), i, actual, signalsPerHour));
            } else {
                System.out.printf("✅ [OK] %s: %d сигналов%n", hours.get(i), actual);
            }
        }

        // Проверка, что оставшиеся часы не изменены
        List<Integer> finalActuals = planner.getOrCreateShift(testDate).getHourlyActualValues();
        for (int i = 8; i < finalActuals.size(); i++) {
            assertThat(finalActuals.get(i))
                    .as("⛔ Час " + hours.get(i) + " (index=" + i + ") не должен быть изменён")
                    .isEqualTo(0);
        }
    }
}
