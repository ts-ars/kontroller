package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class ShiftPlannerUseCase {

    private final ActualDataPort actualDataPort;

    public ShiftPlannerUseCase(ActualDataPort actualDataPort) {
        this.actualDataPort = actualDataPort;
    }

    public void registerProduct(LocalDateTime detectedAt) {
        LocalDate date = detectedAt.toLocalDate(); // используем дату из времени

        Optional<Shift> current = actualDataPort.findByDate(date);
        if (current.isEmpty()) {
            throw new IllegalStateException("No shift for date: " + date);
        }

        Shift old = current.get();
        Shift updated = old.withActual(old.getActual() + 1);
        actualDataPort.save(updated);

        log.info("[SHIFT] Product registered at {} → shift date: {}, new actual: {}",
                detectedAt, date, updated.getActual());
    }
}
