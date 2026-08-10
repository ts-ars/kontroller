package com.exempal.shiftcounter.features.shift.application;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionDayServiceTest {
    private final ProductionDayService service = new ProductionDayService(
            Clock.fixed(Instant.parse("2026-08-10T05:00:00Z"), ZoneOffset.UTC));

    @Test
    void boundaryAtSevenStartsNewProductionDay() {
        assertThat(service.resolve(LocalDateTime.of(2026, 8, 10, 6, 59, 59)).date())
                .isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(service.resolve(LocalDateTime.of(2026, 8, 10, 7, 0)).date())
                .isEqualTo(LocalDate.of(2026, 8, 10));
    }

    @Test
    void productionWindowIsHalfOpen() {
        var day = service.resolve(LocalDateTime.of(2026, 8, 10, 7, 0));
        assertThat(day.contains(day.start())).isTrue();
        assertThat(day.contains(day.end().minusNanos(1))).isTrue();
        assertThat(day.contains(day.end())).isFalse();
    }
}
