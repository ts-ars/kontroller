package com.exempal.shiftcounter.features.shift.infrastructure;

import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class JpaShiftAdapter implements ActualDataPort {

    private final ShiftJpaRepository repository;

    public JpaShiftAdapter(ShiftJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Shift shift) {
        ShiftEntity entity = ShiftEntity.fromDomain(shift);
        repository.save(entity);
    }

    @Override
    public Optional<Shift> findByDate(LocalDate date) {
        return repository.findByDate(date)
                .map(ShiftEntity::toDomain);
    }

    @Override
    public List<Integer> getHourlyActuals(LocalDate date) {
        return repository.findByDate(date)
                .map(entity -> List.of(entity.getActual())) // ← сейчас просто 1 значение
                .orElse(List.of());
    }

    @Override
    public void incrementHourlyActual(LocalDate date, LocalTime time) {
        ShiftEntity entity = repository.findByDate(date)
                .orElseThrow(() -> new IllegalStateException("Shift not found for date: " + date));

        entity.setActual(entity.getActual() + 1);
        repository.save(entity);
    }
}
