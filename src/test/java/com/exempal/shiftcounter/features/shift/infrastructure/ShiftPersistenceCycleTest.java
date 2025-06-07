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
public class ShiftPersistenceCycleTest {

    @Autowired
    private JpaShiftAdapter adapter;

    @Test
    void shouldPersistAndLoadExactShift() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 4);
        Shift original = ShiftTestFactory.with(date, 200, 180, "Недовыполнение");

        // when
        adapter.save(original);
        Shift reloaded = adapter.findByDate(date).orElseThrow();

        // then
        assertThat(reloaded.date()).isEqualTo(original.date());
        assertThat(reloaded.planned()).isEqualTo(original.planned());
        assertThat(reloaded.actual()).isEqualTo(original.actual());
        assertThat(reloaded.comment()).isEqualTo(original.comment());
        assertThat(reloaded.deviation()).isEqualTo(original.deviation());
    }
}
