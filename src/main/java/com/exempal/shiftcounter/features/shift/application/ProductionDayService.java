package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.ProductionDay;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ProductionDayService {
    private final Clock clock;

    public ProductionDayService(Clock clock) {
        this.clock = clock;
    }

    public ProductionDay resolve(LocalDateTime timestamp) {
        LocalDate date = timestamp.toLocalDate();
        if (timestamp.toLocalTime().isBefore(ProductionDay.BOUNDARY)) date = date.minusDays(1);
        return ProductionDay.of(date);
    }

    public ProductionDay current() {
        return resolve(LocalDateTime.now(clock));
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
