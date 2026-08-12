package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.adapter.persistence.JpaShiftAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShiftProductRegistrarTest {

    @Autowired
    private ShiftProductRegistrar productRegistrar;

    @Autowired
    private ShiftInitializerService shiftInitializer;

    @Autowired
    private JpaShiftAdapter shiftAdapter;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        shiftInitializer.createNewShift(today);
    }

    @Test
    void eachSignalShouldGoToItsOwnTimeSlot() {
        // simulate 2 signals for the approved 11:30 interval
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(11, 32)));
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(11, 59)));

        // simulate 2 signals for hour 12:30
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 30)));
        productRegistrar.registerProduct(LocalDateTime.of(today, LocalTime.of(12, 45)));

        // fetch updated shift
        Shift shift = shiftAdapter.findByDate(today).orElseThrow();

        List<String> labels = shift.getHourlyLabels(); // ["08:00", "09:00", ..., "15:30"]
        List<Integer> actuals = shift.getHourlyActualValues();

        int index11 = labels.indexOf("11:30");
        int index1230 = labels.indexOf("12:30");

        assertThat(actuals.get(index11)).isEqualTo(2);
        assertThat(actuals.get(index1230)).isEqualTo(2);
    }
}
