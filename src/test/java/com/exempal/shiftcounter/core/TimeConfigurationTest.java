package com.exempal.shiftcounter.core;

import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeConfigurationTest {
    private final TimeConfiguration configuration = new TimeConfiguration();

    @Test
    void createsApplicationClockInConfiguredZone() {
        var clock = configuration.applicationClock("Europe/Warsaw");

        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Europe/Warsaw"));
    }

    @Test
    void rejectsInvalidConfiguredZone() {
        assertThatThrownBy(() -> configuration.applicationClock("not-a-time-zone"))
                .isInstanceOf(DateTimeException.class);
    }

    @Test
    void warsawClockResolvesProductionDayAcrossSevenOClockBoundary() {
        var zone = ZoneId.of("Europe/Warsaw");
        var beforeBoundary = new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-10T04:59:59Z"), zone));
        var atBoundary = new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-10T05:00:00Z"), zone));

        assertThat(beforeBoundary.current().date()).hasToString("2026-08-09");
        assertThat(atBoundary.current().date()).hasToString("2026-08-10");
    }
}
