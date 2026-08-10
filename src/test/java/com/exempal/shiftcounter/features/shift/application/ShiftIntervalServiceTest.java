package com.exempal.shiftcounter.features.shift.application;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiftIntervalServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 10);
    private final ShiftIntervalService service = new ShiftIntervalService();

    @Test
    void resolvesCrossMidnightAndNinetyMinuteIntervals() {
        var intervals = service.resolve(DATE, List.of("22:30", "00:00", "01:00"), 3);
        assertThat(intervals.get(0).duration()).isEqualTo(Duration.ofMinutes(90));
        assertThat(intervals.get(1).start()).isEqualTo(LocalDateTime.of(2026, 8, 11, 0, 0));
        assertThat(intervals.get(1).duration()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void finalHalfHourEndsAtNextWholeHour() {
        var interval = service.resolve(DATE, List.of("15:30"), 1).getFirst();
        assertThat(interval.end()).isEqualTo(LocalDateTime.of(2026, 8, 10, 16, 0));
    }

    @Test
    void exactBoundaryBelongsToNextInterval() {
        var interval = service.find(DATE, List.of("08:00", "09:00"), 2,
                LocalDateTime.of(2026, 8, 10, 9, 0)).orElseThrow();
        assertThat(interval.index()).isEqualTo(1);
    }

    @Test
    void extensionCreatesPlanRequiredIntervalsAndStopsAtSeven() {
        var labels = service.extendUntil(DATE, List.of("05:30"), 1,
                LocalDateTime.of(2026, 8, 11, 6, 45));
        var interval = service.find(DATE, labels, 1,
                LocalDateTime.of(2026, 8, 11, 6, 45)).orElseThrow();
        assertThat(interval.planSupplied()).isFalse();
        assertThat(interval.end()).isEqualTo(LocalDateTime.of(2026, 8, 11, 7, 0));
        assertThat(service.extendUntil(DATE, labels, 1, LocalDateTime.of(2026, 8, 11, 7, 0)))
                .isEqualTo(labels);
    }

    @Test
    void rejectsDuplicateOrOutOfOrderProductionTimes() {
        assertThatThrownBy(() -> service.resolve(DATE, List.of("08:00", "08:00"), 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.resolve(DATE, List.of("09:00", "08:00"), 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.resolve(DATE, List.of("08:00 garbage"), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.resolve(DATE, List.of("24:00"), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
