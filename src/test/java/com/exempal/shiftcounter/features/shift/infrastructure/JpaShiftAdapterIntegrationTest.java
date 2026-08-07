package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class JpaShiftAdapterIntegrationTest {

    @Autowired
    private JpaShiftAdapter jpaShiftAdapter;

    @Test
    void shouldSaveAndLoadShiftByDate() {
        LocalDate date = LocalDate.of(2025, 6, 3);
        List<Integer> plan = List.of(100, 100, 100);
        List<Integer> actual = List.of(30, 25, 25);
        List<String> labels = List.of("08:00", "09:00", "10:00");

        Shift shift = new Shift(date, plan, 80, actual, labels);
        jpaShiftAdapter.save(shift);

        Shift loaded = jpaShiftAdapter.findByDate(date).orElseThrow();

        assertThat(loaded.getDate()).isEqualTo(date);
        assertThat(loaded.getActual()).isEqualTo(80);
        assertThat(loaded.getId()).isNotNull();
    }

    @Test
    void shouldIncrementActualSafelyThroughSave() {
        LocalDate date = LocalDate.of(2025, 6, 8);
        List<Integer> plan = List.of(100, 100, 100);
        List<Integer> actual = List.of(20, 30, 30);
        List<String> labels = List.of("08:00", "09:00", "10:00");

        Shift shift = new Shift(date, plan, 80, actual, labels);
        jpaShiftAdapter.save(shift);

        // 📦 Инкриминируем 3-й час (index = 2)
        Shift loaded = jpaShiftAdapter.findByDate(date).orElseThrow();
        Shift updated = loaded.withIncrementedHourlyActualValue(2); // индекс 2 = "10:00"
        jpaShiftAdapter.save(updated);

        Shift result = jpaShiftAdapter.findByDate(date).orElseThrow();
        assertThat(result.getHourlyActualValues().get(2)).isEqualTo(31);
        assertThat(result.getActual()).isEqualTo(81); // было 80 → +1
    }
}
