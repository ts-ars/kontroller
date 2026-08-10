package com.exempal.shiftcounter.features.shift;

import com.exempal.shiftcounter.features.shift.application.ShiftInitializerService;
import com.exempal.shiftcounter.features.shift.application.ShiftProductRegistrar;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.adapter.persistence.JpaShiftAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ShiftSignalAssignmentTest {

    @Autowired
    ShiftInitializerService shiftInitializer;

    @Autowired
    ShiftProductRegistrar productRegistrar;

    @Autowired
    JpaShiftAdapter shiftAdapter;

    private static final Logger log = LoggerFactory.getLogger(ShiftSignalAssignmentTest.class);

    @Test
    void eachSignalShouldGoToItsOwnTimeSlot() {
        LocalDate today = LocalDate.now();
        shiftInitializer.createNewShift(today);

        // Сигналы в 2 интервала
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(11, 2)));  // → 11:00
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(11, 59))); // → 11:00

        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 30))); // → 12:30
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 45))); // → 12:30

        Shift shift = shiftAdapter.findByDate(today).orElseThrow();
        List<String> labels = shift.getHourlyLabels(); // ["08:00", ..., "15:30"]
        List<Integer> actuals = shift.getHourlyActualValues(); // [0, 0, ..., N]

        int index11 = labels.indexOf("11:00");
        int index1230 = labels.indexOf("12:30");

        assertEquals(2, actuals.get(index11), "11:00 должен содержать 2 сигнала");
        assertEquals(2, actuals.get(index1230), "12:30 должен содержать 2 сигнала");

        for (int i = 0; i < actuals.size(); i++) {
            if (i != index11 && i != index1230) {
                assertEquals(0, actuals.get(i), "Ожидается 0 в остальных интервалах");
            }
        }
    }

    @Test
    void boundaryTimesShouldMapToCorrectHourSlot() {
        LocalDate today = LocalDate.now();
        shiftInitializer.createNewShift(today);

        // Граничные сигналы
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 29, 59))); // → до 12:30
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 30, 0)));  // → 12:30
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 30, 1)));  // → 12:30

        Shift shift = shiftAdapter.findByDate(today).orElseThrow();
        List<String> rawLabels = shift.getHourlyLabels();        // строки
        List<LocalTime> labels = parseLabels(rawLabels);         // LocalTime
        List<Integer> actuals = shift.getHourlyActualValues();   // значения

        int indexBefore = findSlotIndex(LocalTime.of(12, 29, 59), labels);
        int index1230 = findSlotIndex(LocalTime.of(12, 30), labels);

        assert indexBefore != -1 : "⛔ Не удалось найти интервал для 12:29:59";
        assert index1230 != -1 : "⛔ Не удалось найти интервал для 12:30";

        // вычисляем диапазоны интервалов
        LocalTime startBefore = labels.get(indexBefore);
        LocalTime endBefore = (indexBefore < labels.size() - 1)
                ? labels.get(indexBefore + 1)
                : startBefore.plusHours(1);

        LocalTime start1230 = labels.get(index1230);
        LocalTime end1230 = (index1230 < labels.size() - 1)
                ? labels.get(index1230 + 1)
                : start1230.plusHours(1);

        assertEquals(1, actuals.get(indexBefore),
                "12:29:59 должен попасть в интервал " + startBefore + "–" + endBefore);

        assertEquals(2, actuals.get(index1230),
                "12:30:00 и 12:30:01 должны попасть в интервал " + start1230 + "–" + end1230);

        for (int i = 0; i < actuals.size(); i++) {
            if (i != indexBefore && i != index1230) {
                assertEquals(0, actuals.get(i), "Ожидается 0 в остальных интервалах (index = " + i + ")");
            }
        }
    }

    private int findSlotIndex(LocalTime time, List<LocalTime> hourLabels) {
        for (int i = 0; i < hourLabels.size(); i++) {
            LocalTime start = hourLabels.get(i);
            LocalTime end = hourLabels.get(i + 1);
            if (!time.isBefore(start) && time.isBefore(end)) {
                log.info("🔍 Время {} попало в интервал {}–{} (index = {})", time, start, end, i);
                return i;
            }
        }
        log.warn("⚠️ Время {} не попало ни в один интервал!", time);
        return -1;
    }

    private List<LocalTime> parseLabels(List<String> labels) {
        return labels.stream()
                .map(LocalTime::parse)
                .toList();
    }

    @BeforeEach
    void cleanBeforeTest() {
        shiftAdapter.deleteByDate(LocalDate.now());
    }
}
