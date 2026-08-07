package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class ShiftPersistenceCycleTest {

    @Autowired
    private JpaShiftAdapter jpaShiftAdapter;

    @Test
    void shouldPersistAndLoadExactShift() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 6, 5);
        List<Integer> plan = List.of(90);
        List<Integer> actual = List.of(90);
        List<String> comments = List.of("autotest");
        List<String> labels = List.of("08:00");

        Shift shift = new Shift(date, plan, 90, actual, labels);

        // Act
        jpaShiftAdapter.saveOrReplace(shift); // ✅ безопасная замена по дате
        Shift loaded = jpaShiftAdapter.findByDate(date).orElseThrow();

        // Assert
        assertThat(loaded.getDate()).isEqualTo(date);
        assertThat(loaded.getActual()).isEqualTo(90);
        assertThat(loaded.getHourlyPlanValues()).isEqualTo(plan);
        assertThat(loaded.getId()).isNotNull();
    }

    @Test
    void shouldReplacePreviousShiftOnSameDate() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 6, 6);

        Shift initial = new Shift(date, List.of(80), 80, List.of(80), List.of("08:00"));
        Shift updated = new Shift(date, List.of(120), 120, List.of(120), List.of("08:00"));

        jpaShiftAdapter.saveOrReplace(initial);
        jpaShiftAdapter.saveOrReplace(updated);

        // Act
        Shift loaded = jpaShiftAdapter.findByDate(date).orElseThrow();

        // Assert
        assertThat(loaded.getActual()).isEqualTo(120);
        assertThat(loaded.getHourlyPlanValues()).isEqualTo(List.of(120));
    }
}
