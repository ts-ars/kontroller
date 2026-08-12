package com.exempal.shiftcounter.features.shift.application.projection;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftSliceTest {
    private final LocalDate date = LocalDate.of(2026, 8, 7);

    @Test
    void usesHalfOpenDayAndEveningSlicesAcrossMidnight() {
        assertThat(ShiftSlice.DAY.contains(date, at(7, 0))).isTrue();
        assertThat(ShiftSlice.DAY.contains(date, at(14, 59))).isTrue();
        assertThat(ShiftSlice.DAY.contains(date, at(15, 0))).isFalse();

        assertThat(ShiftSlice.EVENING.contains(date, at(15, 0))).isTrue();
        assertThat(ShiftSlice.EVENING.contains(date, at(23, 30))).isTrue();
        assertThat(ShiftSlice.EVENING.contains(date, date.plusDays(1).atTime(6, 59))).isTrue();
        assertThat(ShiftSlice.EVENING.contains(date, date.plusDays(1).atTime(7, 0))).isFalse();
    }

    private LocalDateTime at(int hour, int minute) { return date.atTime(hour, minute); }
}
