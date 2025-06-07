package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaShiftAdapterIntegrationTest {

    @Autowired
    private JpaShiftAdapter jpaShiftAdapter;

    @Test
    void shouldSaveAndLoadShiftByDate() {
        LocalDate date = LocalDate.of(2025, 6, 3);
        Shift shift = ShiftTestFactory.with(date, 100, 80, "test comment");

        jpaShiftAdapter.save(shift);
        Shift loaded = jpaShiftAdapter.findByDate(date).orElseThrow();

        assertThat(loaded.date()).isEqualTo(date);
        assertThat(loaded.planned()).isEqualTo(100);
        assertThat(loaded.actual()).isEqualTo(80);
        assertThat(loaded.comment()).isEqualTo("test comment");
        assertThat(loaded.deviation()).isEqualTo(-20);
    }
}
